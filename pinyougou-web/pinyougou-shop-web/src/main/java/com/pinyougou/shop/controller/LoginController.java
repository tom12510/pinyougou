package com.pinyougou.shop.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 登录控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-07-17<p>
 */
@Controller
public class LoginController {

    /** 登录方法 */
    @PostMapping("/login")
    public String login(String username, String password){
        try {
            // 获取认证主体对象
            Subject subject = SecurityUtils.getSubject();
            // 创建用户名与密码的令牌
            UsernamePasswordToken token = new UsernamePasswordToken(username, password);
            // 身份认证
            subject.login(token);

            // 判断是否登录成功
            if (subject.isAuthenticated()){
                // 重定向后台主页
                return "redirect:/admin/index.html";
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return "redirect:/shoplogin.html";
    }

    /** 获取登录用户名 */
    @GetMapping("/findLoginName")
    @ResponseBody
    public String findLoginName(){
        return SecurityUtils.getSubject().getPrincipal().toString();
    }
}
