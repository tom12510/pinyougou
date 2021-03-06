package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.Order;
import com.pinyougou.pojo.PayLog;
import com.pinyougou.service.OrderService;
import com.pinyougou.service.WeixinPayService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-04<p>
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Reference(timeout = 10000)
    private OrderService orderService;
    @Reference(timeout = 10000)
    private WeixinPayService weixinPayService;

    /** 提交订单 */
    @PostMapping("/submitOrder")
    public boolean saveOrder(@RequestBody Order order, HttpServletRequest request){
        try {
            // 获取登录用户名
            String userId = request.getRemoteUser();
            // 设置订单关联的用户id
            order.setUserId(userId);

            orderService.save(order);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /** 生成支付二维码 */
    @GetMapping("/genPayCode")
    public Map<String,String> genPayCode(HttpServletRequest request){
        //  {outTradeNo : '', totalFee : 0, codeUrl : ''}
        // 获取登录用户名
        String userId = request.getRemoteUser();
        // 查询支付日志
        PayLog payLog = orderService.findPayLogFromRedis(userId);

        return weixinPayService.genPayCode(payLog.getOutTradeNo(),
                String.valueOf(payLog.getTotalFee()));
    }

    /** 查询支付状态 */
    @GetMapping("/queryPayStatus")
    public Map<String,Integer> queryPayStatus(String outTradeNo){
        Map<String, Integer> data = new HashMap<>();
        data.put("status", 3);
        try{
            Map<String,String> resMap = weixinPayService.queryPayStatus(outTradeNo);
            if (resMap != null && resMap.size() > 0){
                // 支付成功
                if ("SUCCESS".equals(resMap.get("trade_state"))){
                    // 支付成功后，处理业务
                    orderService.updateOrderStatus(outTradeNo, resMap.get("transaction_id"));
                    data.put("status", 1);
                }
                // 未支付
                if ("NOTPAY".equals(resMap.get("trade_state"))){
                    data.put("status", 2);
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return data;
    }

}
