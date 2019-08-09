package com.pinyougou.service;

import com.pinyougou.cart.Cart;

import java.util.List; /**
 * CartService接口
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-03<p>
 */
public interface CartService {

    /**
     * 添加商品到购物车
     * @param carts 用户的购物车
     * @param itemId SKU商品的id
     * @param num 购买数量
     * @return 修改后的购物车
     */
    List<Cart> addItemToCart(List<Cart> carts, Long itemId, Integer num);

    /**
     * 添加用户购物车到Redis数据库
     * @param userId
     * @param carts
     */
    void saveCartRedis(String userId, List<Cart> carts);

    /**
     * 从Redis数据库获取用户的购物车
     * @param userId
     * @return
     */
    List<Cart> findCartRedis(String userId);

    /**
     * 把Cookie中购物车合并到Redis,返回修改后的购物车
     * @param redisCarts
     * @param cookieCarts
     * @return 合并后的购物车
     */
    List<Cart> mergeCart(List<Cart> redisCarts, List<Cart> cookieCarts);
}
