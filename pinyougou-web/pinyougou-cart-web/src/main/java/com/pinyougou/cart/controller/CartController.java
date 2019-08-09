package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.Cart;
import com.pinyougou.common.util.CookieUtils;
import com.pinyougou.service.CartService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 购物车控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-03<p>
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference(timeout = 10000)
    private CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Autowired(required = false)
    private HttpServletResponse response;

    /** 添加商品到购物车 */
    @GetMapping("/addCart")
    @CrossOrigin(origins = {"http://item.pinyougou.com"},
            allowCredentials = "true")
    public boolean addCart(Long itemId, Integer num){
        try{
//            // 设置允许访问的域名(跨域的域名) 99%
//            response.setHeader("Access-Control-Allow-Origin", "http://item.pinyougou.com");
//            // 设置允许Cookie跨域访问 1%
//            response.setHeader("Access-Control-Allow-Credentials","true");

            // 1. 获取用户原来的购物车
            List<Cart> carts = findCart();

            // 2. 添加商品到用户原来购物车
            carts = cartService.addItemToCart(carts, itemId, num);

            // 3. 获取登录用户名
            String userId = request.getRemoteUser();
            if (StringUtils.isNoneBlank(userId)){ // 已登录
                // 3.1 把修改后的购物车存储到Redis
                cartService.saveCartRedis(userId, carts);

            }else { // 未登录
                // 3.2 把修改后的购物车存储到Cookie
                CookieUtils.setCookie(request, response,
                        CookieUtils.CookieName.PINYOUGOU_CART,
                        JSON.toJSONString(carts),
                        60 * 60 * 24, true);
            }
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /** 查询用户的购物车 */
    @GetMapping("/findCart")
    public List<Cart> findCart(){

        List<Cart> carts = null;
        // 获取登录用户名
        String userId = request.getRemoteUser();
        if (StringUtils.isNoneBlank(userId)){ // 已登录
            /** ############ 已登录的用户，从Redis获取购物车数据 ############### */
            carts = cartService.findCartRedis(userId);

            /** ######### 购物车合并 ########### */
            // 1. 从cookie获取用户的购物车
            String cartJsonStr = CookieUtils.getCookieValue(request,
                    CookieUtils.CookieName.PINYOUGOU_CART, true);
            if (StringUtils.isNoneBlank(cartJsonStr)){
                // 把json数组格式的字符串转化成List集合
                List<Cart> cookieCarts = JSON.parseArray(cartJsonStr, Cart.class);
                if (cookieCarts.size() > 0){
                    // 把Cookie中购物车合并到Redis,返回修改后的购物车
                    carts = cartService.mergeCart(carts, cookieCarts);
                    // 把合并后的购物车同步到Redis
                    cartService.saveCartRedis(userId, carts);
                    // 删除Cookie
                    CookieUtils.deleteCookie(request, response,
                            CookieUtils.CookieName.PINYOUGOU_CART);
                }
            }


        }else{ // 未登录
            /** ############ 未登录的用户，从Cookie获取购物车数据 ############### */
            // List<Cart> 的json字符串
            String cartJsonStr = CookieUtils.getCookieValue(request,
                    CookieUtils.CookieName.PINYOUGOU_CART, true);
            if (StringUtils.isBlank(cartJsonStr)){
                // 创建新的购物车
                cartJsonStr = "[]";
            }
            carts = JSON.parseArray(cartJsonStr, Cart.class);
        }
        return carts;
    }

}
