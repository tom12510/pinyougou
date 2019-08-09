package com.pinyougou.seckill.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.SeckillGoodsMapper;
import com.pinyougou.mapper.SeckillOrderMapper;
import com.pinyougou.pojo.SeckillGoods;
import com.pinyougou.pojo.SeckillOrder;
import com.pinyougou.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀订单服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-07<p>
 */
@Service
@Transactional
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private IdWorker idWorker;

    @Override
    public void save(SeckillOrder seckillOrder) {

    }

    @Override
    public void update(SeckillOrder seckillOrder) {

    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {

    }

    @Override
    public SeckillOrder findOne(Serializable id) {
        return null;
    }

    @Override
    public List<SeckillOrder> findAll() {
        return null;
    }

    @Override
    public List<SeckillOrder> findByPage(SeckillOrder seckillOrder, int page, int rows) {
        return null;
    }

    /**
     * 秒杀下单
     * 线程安全(线程锁) synchronized (单进程，多线程)
     * 多进程安全(多条线程) 分布式锁
     * */
    public synchronized void submitOrderToRedis(String userId, Long id){
        try{
            // 获取Redis分布式锁，如果为true代表获取锁成功
            boolean lock = redisTemplate.opsForValue().setIfAbsent("kill_" + id, true);
            if (lock){
                // 自动解锁的时间
                redisTemplate.expire("kill_" + id, 10, TimeUnit.SECONDS);

                // 从Redis数据库中获取秒杀商品
                SeckillGoods seckillGoods = (SeckillGoods) redisTemplate
                        .boundHashOps("seckillGoodsList").get(id);
                if (seckillGoods != null && seckillGoods.getStockCount() > 0){
                    // 减库存(线程安全)
                    seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);

                    // 判断是否被秒光了
                    if (seckillGoods.getStockCount() == 0){
                        // 把秒杀商品同步到数据库
                        seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
                        // 从Redis数据库中删除秒杀商品
                        redisTemplate.boundHashOps("seckillGoodsList").delete(id);
                    }else{
                       // 同步秒杀商品到Redis
                       redisTemplate.boundHashOps("seckillGoodsList").put(id, seckillGoods);
                    }

                    // 产生秒杀订单
                    SeckillOrder seckillOrder = new SeckillOrder();
                    // 主键id
                    seckillOrder.setId(idWorker.nextId());
                    // 秒杀商品id
                    seckillOrder.setSeckillId(id);
                    // 秒杀订单金额
                    seckillOrder.setMoney(seckillGoods.getCostPrice());
                    // 关联的用户
                    seckillOrder.setUserId(userId);
                    // 商家id
                    seckillOrder.setSellerId(seckillGoods.getSellerId());
                    // 创建时间
                    seckillOrder.setCreateTime(new Date());
                    // 支付状态
                    seckillOrder.setStatus("0");
                    // 秒杀订单存入Redis数据库
                    redisTemplate.boundHashOps("seckillOrderList").put(userId, seckillOrder);
                }
            }else{
                throw new RuntimeException("秒杀失败！");
            }
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }finally {
            // 释放分布式锁(怕死锁)
            redisTemplate.delete("kill_" + id);
        }
    }

    /** 获取用户的秒杀订单 */
    public SeckillOrder findSeckillOrderFromRedis(String userId){
        try{
            return (SeckillOrder)redisTemplate
                    .boundHashOps("seckillOrderList").get(userId);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 把秒杀订单同步到数据库 */
    public void saveOrder(String userId, String transactionId){
        try{
            // 1. 从Redis中获取秒杀订单
            SeckillOrder seckillOrder = (SeckillOrder) redisTemplate
                    .boundHashOps("seckillOrderList").get(userId);

            // 2. 同步到数据库
            seckillOrder.setTransactionId(transactionId);
            seckillOrder.setPayTime(new Date());
            seckillOrder.setStatus("1");
            seckillOrderMapper.insertSelective(seckillOrder);

            // 3. 从Redis中删除秒杀订单
            redisTemplate.boundHashOps("seckillOrderList").delete(userId);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 查询全部超时未支付的秒杀订单 */
    public List<SeckillOrder> findOrderByTimeout(){
        try{
            List<SeckillOrder> seckillOrders = new ArrayList<>();
            // 从Redis获取全部未付的秒杀订单
            List<SeckillOrder> seckillOrderList = redisTemplate
                    .boundHashOps("seckillOrderList").values();
            for (SeckillOrder seckillOrder : seckillOrderList) {
                // 定义时间系统 - 5分钟的毫秒数
                long time = new Date().getTime() - 5 * 60 * 1000;
                if (seckillOrder.getCreateTime().getTime() < time){
                    seckillOrders.add(seckillOrder);
                }
            }
            return seckillOrders;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 从Redis中删除秒杀订单，恢复库存 */
    public void deleteOrderFromRedis(SeckillOrder seckillOrder){
        try{
            // 1. 从Redis中删除秒杀订单
            redisTemplate.boundHashOps("seckillOrderList")
                    .delete(seckillOrder.getUserId());

            // 2. 恢复Redis中秒杀商品的库存
            // 2.1 从Redis中获取该秒杀商品
            SeckillGoods seckillGoods = (SeckillGoods)redisTemplate
                    .boundHashOps("seckillGoodsList")
                    .get(seckillOrder.getSeckillId());
            if (seckillGoods != null){
                // 增加库存
                seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
            }else{
               // 从数据库中查询秒杀商品
                seckillGoods = seckillGoodsMapper
                        .selectByPrimaryKey(seckillOrder.getSeckillId());
                seckillGoods.setStockCount(1);
            }
            // 重新把秒杀商品存入Redis
            redisTemplate.boundHashOps("seckillGoodsList")
                    .put(seckillGoods.getId(), seckillGoods);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
