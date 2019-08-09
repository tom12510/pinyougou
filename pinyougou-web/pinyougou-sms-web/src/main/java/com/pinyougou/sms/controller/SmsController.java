package com.pinyougou.sms.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.service.SmsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 短信控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-07-31<p>
 */
@RestController
@RequestMapping("/sms")
public class SmsController {

    @Reference(timeout = 10000)
    private SmsService smsService;

    /** 短信发送 */
    @PostMapping("/sendSms")
    public Map<String,Object> sendSms(String phone, String signName,
                                      String templateCode , String templateParam){
        // 调用服务接口发送短信
        boolean success = smsService.sendSms(phone, signName, templateCode, templateParam);

        Map<String,Object> data = new HashMap<>();
        data.put("success", success);
        return data;
    }

}
