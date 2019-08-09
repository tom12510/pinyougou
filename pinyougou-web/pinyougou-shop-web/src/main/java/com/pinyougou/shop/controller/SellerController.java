package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.Seller;
import com.pinyougou.service.SellerService;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-07-17<p>
 */
@RestController
@RequestMapping("/seller")
public class SellerController {

    @Reference(timeout = 10000)
    private SellerService sellerService;

    /** 商家申请入驻 */
    @PostMapping("/save")
    public boolean save(@RequestBody Seller seller){
        try {
            // 密码加密、加盐、加迭代次数
            // String algorithmName, Object source, Object salt, int hashIterations
            String password = new SimpleHash("md5", // 加密名称
                    seller.getPassword(), // 明文
                    seller.getSellerId(), // 盐
                    5).toHex(); // 迭代次数
            seller.setPassword(password);
            sellerService.save(seller);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

}
