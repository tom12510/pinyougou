package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.common.util.HttpClientUtils;
import com.pinyougou.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-05<p>
 */
@Service
public class WeixinPayServiceImpl implements WeixinPayService {

    // 微信公众账号或开放平台APP的唯一标识
    @Value("${appid}")
    private String appid;
    // 商户账号
    @Value("${partner}")
    private String partner;
    // 商户密钥
    @Value("${partnerkey}")
    private String partnerkey;

    // 统一下单接口URL
    @Value("${unifiedorder}")
    private String unifiedorder;
    // 查询订单接口URL
    @Value("${orderquery}")
    private String orderquery;
    // 关闭订单接口URL
    @Value("${closeorder}")
    private String closeorder;

    /**
     * 调用微信支付系统的“统一下单”接口
     * 获取code_url生成支付二维码
     */
    public Map<String,String> genPayCode(String outTradeNo, String totalFee){
        try{
            // 1. 定义Map集合封装接口需要请求参数
            Map<String,String> params = new HashMap<>();
            // 公众账号ID	 appid	是
            params.put("appid",appid);
            // 商户号	mch_id	是
            params.put("mch_id", partner);
            // 随机字符串	 nonce_str	是
            params.put("nonce_str", WXPayUtil.generateNonceStr());
            // 商品描述	body	是
            params.put("body","品优购");
            // 商户订单号	out_trade_no	是
            params.put("out_trade_no", outTradeNo);
            // 标价金额	total_fee	是 (订单总金额，单位为分)
            params.put("total_fee",totalFee);
            // 终端IP	spbill_create_ip	是
            params.put("spbill_create_ip", "127.0.0.1");
            // 通知地址	notify_url	是
            params.put("notify_url","http://www.pinyougou.com");
            // 交易类型	trade_type	是 NATIVE -Native支付
            params.put("trade_type","NATIVE");

            // 生成带签名的xml格式的字符串
            String xmlParam = WXPayUtil.generateSignedXml(params, partnerkey);
            System.out.println("请求参数：" + xmlParam);

            // 2. 用httpclient调用接口
            HttpClientUtils httpClientUtils = new HttpClientUtils(true);
            String xmlData = httpClientUtils.sendPost(unifiedorder, xmlParam);
            System.out.println("响应数据：" + xmlData);

            // 3. 获取响应数据，返回
            // 把xml格式字符串转化成Map
            Map<String, String> resMap = WXPayUtil.xmlToMap(xmlData);

            // {outTradeNo : '', totalFee : 0, codeUrl : ''}
            Map<String,String> data = new HashMap<>();
            data.put("outTradeNo", outTradeNo);
            data.put("totalFee", totalFee);
            // 二维码链接	code_url
            data.put("codeUrl", resMap.get("code_url"));

            return data;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /**
     * 调用微信支付系统的“查询订单”接口
     * 获取trade_state支付状态码
     */
    public Map<String,String> queryPayStatus(String outTradeNo){
        try{
            // 1. 定义Map集合封装接口需要请求参数
            Map<String,String> params = new HashMap<>();
            // 公众账号ID	 appid	是
            params.put("appid",appid);
            // 商户号	mch_id	是
            params.put("mch_id", partner);
            // 商户订单号	out_trade_no	是
            params.put("out_trade_no", outTradeNo);
            // 随机字符串	 nonce_str	是
            params.put("nonce_str", WXPayUtil.generateNonceStr());

            // 生成带签名的xml格式的字符串
            String xmlParam = WXPayUtil.generateSignedXml(params, partnerkey);
            System.out.println("请求参数：" + xmlParam);

            // 2. 用httpclient调用接口
            HttpClientUtils httpClientUtils = new HttpClientUtils(true);
            String xmlData = httpClientUtils.sendPost(orderquery, xmlParam);
            System.out.println("响应数据：" + xmlData);

            // 3. 获取响应数据，返回
            // 把xml格式字符串转化成Map
            return WXPayUtil.xmlToMap(xmlData);

        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }


    /**
     * 调用微信支付系统的“关闭订单”接口
     * 获取result_code关闭状态码
     */
    public Map<String,String> closePayTimeout(String outTradeNo){
        try{
            // 1. 定义Map集合封装接口需要请求参数
            Map<String,String> params = new HashMap<>();
            // 公众账号ID	 appid	是
            params.put("appid",appid);
            // 商户号	mch_id	是
            params.put("mch_id", partner);
            // 商户订单号	out_trade_no	是
            params.put("out_trade_no", outTradeNo);
            // 随机字符串	 nonce_str	是
            params.put("nonce_str", WXPayUtil.generateNonceStr());

            // 生成带签名的xml格式的字符串
            String xmlParam = WXPayUtil.generateSignedXml(params, partnerkey);
            System.out.println("请求参数：" + xmlParam);

            // 2. 用httpclient调用接口
            HttpClientUtils httpClientUtils = new HttpClientUtils(true);
            String xmlData = httpClientUtils.sendPost(closeorder, xmlParam);
            System.out.println("响应数据：" + xmlData);

            // 3. 获取响应数据，返回
            // 把xml格式字符串转化成Map
            return WXPayUtil.xmlToMap(xmlData);

        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
