package com.pinyougou.service;

import com.pinyougou.pojo.SeckillOrder;
import java.util.List;
import java.io.Serializable;
/**
 * SeckillOrderService 服务接口
 * @date 2019-07-12 15:27:34
 * @version 1.0
 */
public interface SeckillOrderService {

	/** 添加方法 */
	void save(SeckillOrder seckillOrder);

	/** 修改方法 */
	void update(SeckillOrder seckillOrder);

	/** 根据主键id删除 */
	void delete(Serializable id);

	/** 批量删除 */
	void deleteAll(Serializable[] ids);

	/** 根据主键id查询 */
	SeckillOrder findOne(Serializable id);

	/** 查询全部 */
	List<SeckillOrder> findAll();

	/** 多条件分页查询 */
	List<SeckillOrder> findByPage(SeckillOrder seckillOrder, int page, int rows);

	/** 秒杀下单 */
    void submitOrderToRedis(String userId, Long id);

    /** 获取用户的秒杀订单 */
    SeckillOrder findSeckillOrderFromRedis(String userId);

    /** 把秒杀订单同步到数据库 */
	void saveOrder(String userId, String transactionId);

	/** 查询全部超时未支付的秒杀订单 */
    List<SeckillOrder> findOrderByTimeout();

    /** 从Redis中删除秒杀订单，恢复库存 */
	void deleteOrderFromRedis(SeckillOrder seckillOrder);
}