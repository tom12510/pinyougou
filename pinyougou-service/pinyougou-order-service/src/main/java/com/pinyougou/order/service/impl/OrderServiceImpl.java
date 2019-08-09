package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.Cart;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.OrderItemMapper;
import com.pinyougou.mapper.OrderMapper;
import com.pinyougou.mapper.PayLogMapper;
import com.pinyougou.pojo.Order;
import com.pinyougou.pojo.OrderItem;
import com.pinyougou.pojo.PayLog;
import com.pinyougou.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-04<p>
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private PayLogMapper payLogMapper;

    @Override
    public void save(Order order) {
        try{
            // 从Redis中获取用户的购物车（一个Cart对象产生一个订单）
            List<Cart> carts = (List<Cart>)redisTemplate
                    .boundValueOps("cart_" + order.getUserId()).get();

            // 定义支付总金额
            double totalMoney = 0;
            // 定义多个订单id拼接的变量
            StringBuilder orderIds = new StringBuilder();


            // 产生订单
            for (Cart cart : carts) {

                // 创建订单对象
                Order order1 = new Order();
                // 产生订单主键id
                long orderId = idWorker.nextId();
                // 设置订单id
                order1.setOrderId(orderId);
                // 设置支付方式
                order1.setPaymentType(order.getPaymentType());
                // 设置订单的状态码：1、未付款
                order1.setStatus("1");
                // 设置订单的创建时间
                order1.setCreateTime(new Date());
                // 设置订单的更新时间
                order1.setUpdateTime(order1.getCreateTime());
                // 设置订单关联的用户id
                order1.setUserId(order.getUserId());
                // 设置订单的收件地址
                order1.setReceiverAreaName(order.getReceiverAreaName());
                // 设置订单的收件人的手机号码
                order1.setReceiverMobile(order.getReceiverMobile());
                // 设置订单的收件人姓名
                order1.setReceiver(order.getReceiver());
                // 设置订单来源
                order1.setSourceType(order.getSourceType());
                // 设置订单关联的商家id
                order1.setSellerId(cart.getSellerId());

                double money = 0;

                // 循环商家的购物车集合
                for (OrderItem orderItem : cart.getOrderItems()) {
                    // 设置主键id
                    orderItem.setId(idWorker.nextId());
                    // 设置关联的订单id
                    orderItem.setOrderId(orderId);
                    // 统计该订单的总金额
                    money += orderItem.getTotalFee().doubleValue();
                    // 1. 往tb_order_item表插入数据
                    orderItemMapper.insertSelective(orderItem);
                }

                // 多个订单id
                orderIds.append(orderId + ",");
                // 累计多个订单的金额
                totalMoney += money;
                // 设置订单支付的总金额
                order1.setPayment(new BigDecimal(money));
                // 2. 往tb_order表插入数据
                orderMapper.insertSelective(order1);
            }

            // 往tb_pay_log表插入记录
            if ("1".equals(order.getPaymentType())){
                // 创建支付日志对象
                PayLog payLog = new PayLog();
                // 设置交易订单号
                payLog.setOutTradeNo(String.valueOf(idWorker.nextId()));
                // 设置创建时间
                payLog.setCreateTime(new Date());
                // 设置支付金额(分)
                payLog.setTotalFee((long)(totalMoney * 100));
                // 设置支付的用户id
                payLog.setUserId(order.getUserId());
                // 设置支付状态: 未支付
                payLog.setTradeState("0");
                // 设置关联的订单(多个订单)
                payLog.setOrderList(orderIds.toString()
                        .substring(0, orderIds.toString().length() - 1));
                // 设置支付类型
                payLog.setPayType(order.getPaymentType());

                // 插入数据
                payLogMapper.insertSelective(payLog);

                // 把最新的支付存储到Redis
                redisTemplate.boundValueOps("payLog_" + order.getUserId()).set(payLog);
            }

            // 3. 从Redis数据库中删除用户的购物车
            redisTemplate.delete("cart_" + order.getUserId());

        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void update(Order order) {

    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {

    }

    @Override
    public Order findOne(Serializable id) {
        return null;
    }

    @Override
    public List<Order> findAll() {
        return null;
    }

    @Override
    public List<Order> findByPage(Order order, int page, int rows) {
        return null;
    }

    /** 查询支付日志 */
    public PayLog findPayLogFromRedis(String userId){
        try{
            return (PayLog)redisTemplate.boundValueOps("payLog_" + userId).get();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 修改订单状态 */
    public void updateOrderStatus(String outTradeNo, String transactionId){
        try{
            // 1. 修改支付日志表
            PayLog payLog = payLogMapper.selectByPrimaryKey(outTradeNo);
            payLog.setTradeState("1");
            payLog.setPayTime(new Date());
            payLog.setTransactionId(transactionId);
            payLogMapper.updateByPrimaryKeySelective(payLog);

            // 2. 修改订单状态
            String[] orderIds = payLog.getOrderList().split(",");
            for (String orderId : orderIds) {
                Order order = new Order();
                // 主键id
                order.setOrderId(Long.valueOf(orderId));
                // 支付状态
                order.setPaymentTime(payLog.getPayTime());
                // 已付款
                order.setStatus("2");
                orderMapper.updateByPrimaryKeySelective(order);
            }
            // 3. 从Redis中删除支付日志
            redisTemplate.delete("payLog_" + payLog.getUserId());
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

}
