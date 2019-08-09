package com.pinyougou.seckill.task;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.SeckillOrder;
import com.pinyougou.service.SeckillOrderService;
import com.pinyougou.service.WeixinPayService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 任务调度类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-08<p>
 */
@Component
public class SeckillOrderTask {

    @Reference(timeout = 10000)
    private SeckillOrderService seckillOrderService;
    @Reference(timeout = 10000)
    private WeixinPayService weixinPayService;

    /**
     * 任务调度的方法(关闭超时未支付的秒杀订单)
     * cron: 触发任务调度的时间表达式
     * 秒  分  小时  日   月  周
     */
    @Scheduled(cron = "0/3 * * * * *")
    public void closeOrderTask(){
        System.out.println("当前时间：" + new Date());
        // 1. 查询全部超时未支付的秒杀订单
        List<SeckillOrder> seckillOrderList = seckillOrderService.findOrderByTimeout();
        System.out.println("需要关闭的秒杀订单的数量：" + seckillOrderList.size());
        // 2. 调用微信支付系统的“关闭订单”接口，关闭订单
        if (seckillOrderList.size() > 0){
            for (SeckillOrder seckillOrder : seckillOrderList) {
                // 关闭订单
                Map<String,String> resMap = weixinPayService
                        .closePayTimeout(seckillOrder.getId().toString());
                if (resMap != null && resMap.size() > 0){
                    // 判断关单状态
                    if ("SUCCESS".equals(resMap.get("result_code"))){
                        // 3. 从Redis中删除秒杀订单，恢复库存
                        seckillOrderService.deleteOrderFromRedis(seckillOrder);
                    }
                }
            }
        }
    }
}
