package com.pinyougou.cart.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.Cart;
import com.pinyougou.mapper.ItemMapper;
import com.pinyougou.pojo.Item;
import com.pinyougou.pojo.OrderItem;
import com.pinyougou.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-03<p>
 */
@Service
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加商品到购物车
     * @param carts 用户的购物车
     * @param itemId SKU商品的id
     * @param num 购买数量
     * @return 修改后的购物车
     */
    public List<Cart> addItemToCart(List<Cart> carts, Long itemId, Integer num){
        try{
            // 根据itemId从tb_item表查询一条数据
            Item item = itemMapper.selectByPrimaryKey(itemId);
            // 获取商家的id
            String sellerId = item.getSellerId();

            // 从用户的购物车集合中查询到对应商家的购物车
            Cart cart = searchCartBySellerId(carts, sellerId);
            // 判断是否购买过该商家的商品
            if (cart == null){
                // 代表没有买过该商家的商品
                cart = new Cart();
                // 设置商家id
                cart.setSellerId(sellerId);
                // 设置商家的名称
                cart.setSellerName(item.getSeller());

                // 创建商家的商品购物车集合
                List<OrderItem> orderItems = new ArrayList<>();
                // 创建购物车中的商品(把Item转化成OrderItem)
                OrderItem orderItem = createOrderItem(item, num);
                orderItems.add(orderItem);
                // 设置商家的商品购物车集合
                cart.setOrderItems(orderItems);

                // 添加到用户的购物车集合
                carts.add(cart);
            }else{
                // 代表购买过该商家的商品
                // 获取该商家的购物车集合
                List<OrderItem> orderItems = cart.getOrderItems();
                // 从该商家的购物车集合搜索一个商品
                OrderItem orderItem = searchOrderItemByItemId(orderItems, itemId);
                // 判断是否为空
                if (orderItem == null){ // 代表没有买过同样的商品
                    orderItem = createOrderItem(item, num);
                    orderItems.add(orderItem);
                }else {  // 代表买过同样的商品
                    // 设置购买数量相加
                    orderItem.setNum(orderItem.getNum() + num);
                    // 设置小计金额
                    orderItem.setTotalFee(new BigDecimal(orderItem
                            .getPrice().doubleValue() * orderItem.getNum()));

                    // 判断购买数量是否等零
                    if (orderItem.getNum() == 0){
                        // 从商家的购物车中删除商品
                        orderItems.remove(orderItem);
                    }
                    if (orderItems.size() == 0){
                        // 从用户的购物车集合中删除商家的购物车
                        carts.remove(cart);
                    }
                }
            }
            return carts;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 从该商家的购物车集合搜索一个商品 */
    private OrderItem searchOrderItemByItemId(List<OrderItem> orderItems, Long itemId) {
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getItemId().equals(itemId)){
                return orderItem;
            }
        }
        return null;
    }

    /** 创建购物车中的商品(把Item转化成OrderItem) */
    private OrderItem createOrderItem(Item item, Integer num) {
        OrderItem orderItem = new OrderItem();
        // 设置SKU商品的id
        orderItem.setItemId(item.getId());
        // 设置SPU商品的id
        orderItem.setGoodsId(item.getGoodsId());
        // 设置商品的标题
        orderItem.setTitle(item.getTitle());
        // 设置商品的价格
        orderItem.setPrice(item.getPrice());
        // 设置商品的购买数量
        orderItem.setNum(num);
        // 设置小计金额
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));
        // 设置商品图片
        orderItem.setPicPath(item.getImage());
        // 设置商家id
        orderItem.setSellerId(item.getSellerId());
        return orderItem;
    }

    /** 从用户的购物车集合中查询到对应商家的购物车 */
    private Cart searchCartBySellerId(List<Cart> carts, String sellerId) {
        for (Cart cart : carts) {
            if (cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }


    /**
     * 添加用户购物车到Redis数据库
     * @param userId
     * @param carts
     */
    public void saveCartRedis(String userId, List<Cart> carts){
        try{
            redisTemplate.boundValueOps("cart_" + userId).set(carts);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /**
     * 从Redis数据库获取用户的购物车
     * @param userId
     * @return
     */
    public List<Cart> findCartRedis(String userId){
        try{
            List<Cart> carts = (List<Cart>)redisTemplate
                    .boundValueOps("cart_" + userId).get();
            if (carts == null){
                carts = new ArrayList<>();
            }
            return carts;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    /**
     * 把Cookie中购物车合并到Redis,返回修改后的购物车
     * @param redisCarts
     * @param cookieCarts
     * @return 合并后的购物车
     */
    public List<Cart> mergeCart(List<Cart> redisCarts, List<Cart> cookieCarts){
        try{
            // 迭代Cookie中的购物车
            for (Cart cookieCart : cookieCarts) {
                for (OrderItem orderItem : cookieCart.getOrderItems()) {
                    redisCarts = addItemToCart(redisCarts, orderItem.getItemId(), orderItem.getNum());
                }
            }
            return redisCarts;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}