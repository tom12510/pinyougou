package com.pinyougou.sms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.pinyougou.service.SmsService;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

/**
 * 短信服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-07-31<p>
 */
@Service
public class SmsServiceImpl implements SmsService {

    // 产品域名
    private static final String DOMAIN = "dysmsapi.aliyuncs.com";
    // 签名KEY
    @Value("${sms.accessKeyId}")
    private String accessKeyId;
    // 签名密钥
    @Value("${sms.accessKeySecret}")
    private String accessKeySecret;

    /**
     * 发送短信方法
     * @param phone 手机号
     * @param signName 短信签名
     * @param templateCode 模板代号
     * @param templateParam 模板参数json字符串
     * @return true : 成功 false 失败
     */
    public boolean sendSms(String phone, String signName, String templateCode , String templateParam){
        try{
            DefaultProfile profile = DefaultProfile
                    .getProfile("default", accessKeyId, accessKeySecret);
            IAcsClient client = new DefaultAcsClient(profile);
            // 封装请求参数
            CommonRequest request = new CommonRequest();
            // 设置公用请求参数
            request.setMethod(MethodType.POST);
            request.setDomain(DOMAIN);
            request.setVersion("2017-05-25");
            request.setAction("SendSms");
            // 设置短信请求参数
            request.putQueryParameter("PhoneNumbers", phone);
            request.putQueryParameter("SignName", signName);
            request.putQueryParameter("TemplateCode", templateCode);
            request.putQueryParameter("TemplateParam", templateParam);

            CommonResponse response = client.getCommonResponse(request);
            System.out.println("响应数据：" + response.getData());
            Map<String,String> resMap = JSON.parseObject(response.getData(),Map.class);

            return "OK".equals(resMap.get("Code"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}