package com.pinyougou.service;

import java.util.Map;

/**
 * WeixinPayService接口
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-05<p>
 */
public interface WeixinPayService {

    /**
     * 调用微信支付系统的“统一下单”接口
     * 获取code_url生成支付二维码
     */
    Map<String,String> genPayCode(String outTradeNo, String totalFee);

    /**
     * 调用微信支付系统的“查询订单”接口
     * 获取trade_state支付状态码
     */
    Map<String,String> queryPayStatus(String outTradeNo);

    /**
     * 调用微信支付系统的“关闭订单”接口
     * 获取result_code关闭状态码
     */
    Map<String,String> closePayTimeout(String outTradeNo);
}
