package com.pinyougou.portal.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-03<p>
 */
@RestController
public class LoginController {

    @GetMapping("/user/showName")
    public Map<String,String> showName(HttpServletRequest request){
        System.out.println(request.getRequestURL().toString());
        // 获取登录用户名
        String loginName = request.getRemoteUser();
        Map<String,String> data = new HashMap<>();
        data.put("loginName", loginName);
        return data;
    }
}
