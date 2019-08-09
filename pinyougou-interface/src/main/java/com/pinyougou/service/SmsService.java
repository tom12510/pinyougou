package com.pinyougou.service;

/**
 * SmsService接口
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-07-31<p>
 */
public interface SmsService {

    /**
     * 发送短信方法
     * @param phone 手机号
     * @param signName 短信签名
     * @param templateCode 模板代号
     * @param templateParam 模板参数json字符串
     * @return true : 成功 false 失败
     */
    boolean sendSms(String phone, String signName, String templateCode , String templateParam);
}
