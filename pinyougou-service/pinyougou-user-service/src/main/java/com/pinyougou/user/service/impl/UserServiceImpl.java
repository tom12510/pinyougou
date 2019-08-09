package com.pinyougou.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.common.util.HttpClientUtils;
import com.pinyougou.mapper.UserMapper;
import com.pinyougou.pojo.User;
import com.pinyougou.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-07-31<p>
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Value("${sms.url}")
    private String smsUrl;
    @Value("${sms.signName}")
    private String signName;
    @Value("${sms.templateCode}")
    private String templateCode;


    @Override
    public void save(User user) {
        try{
            // 密码需要MD5加密(commons-codec.jar)
            user.setPassword(DigestUtils.md5Hex(user.getPassword()));
            // 注册时间
            user.setCreated(new Date());
            // 修改时间
            user.setUpdated(user.getCreated());
            // 添加
            userMapper.insertSelective(user);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void update(User user) {

    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {

    }

    @Override
    public User findOne(Serializable id) {
        return null;
    }

    @Override
    public List<User> findAll() {
        return null;
    }

    @Override
    public List<User> findByPage(User user, int page, int rows) {
        return null;
    }

    /** 发送短信验证码 */
    public boolean sendSmsCode(String phone){
        try{
            // 1. 随机生成六位数字的验证码
            //String code = (Math.random() + "").replaceAll("\\.", "").substring(0, 6);
            String code = UUID.randomUUID().toString().replaceAll("-","")
                    .replaceAll("[a-z|A-Z]","").substring(0,6);
            System.out.println("code:" + code);

            // 2. 调用短信发送接口
            HttpClientUtils httpClientUtils = new HttpClientUtils(false);
            Map<String,String> params = new HashMap<>();
            params.put("phone", phone);
            params.put("signName", signName);
            params.put("templateCode", templateCode);
            params.put("templateParam", "{'number':"+ code +"}");
            String json = httpClientUtils.sendPost(smsUrl, params);
            System.out.println("json = " + json);

            // 3. 判断短信是否发送成功
            Map<String,Object> map = JSON.parseObject(json, Map.class);
            boolean success = (boolean)map.get("success");

            // 4. 如果发送成功，短信验证码存储到Redis key: phone号码  value: 验证码(过期时间90秒)
            if (success){
                redisTemplate.boundValueOps(phone).set(code, 90, TimeUnit.SECONDS);
            }
            return success;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 检验短信验证码 */
    public boolean checkSmsCode(String phone, String code){
        try{
            String oldCode = redisTemplate.boundValueOps(phone).get();
            return oldCode != null && oldCode.equals(code);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

}
