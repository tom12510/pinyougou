package com.pinyougou.shop.realm;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.Seller;
import com.pinyougou.service.SellerService;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;

/**
 * 自定义认证域
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-07-17<p>
 */
public class SellerAuthorizingRealm extends AuthorizingRealm {

    @Reference(timeout = 10000)
    private SellerService sellerService;

    /** 授权方法 */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }

    /** 身份认证方法 (subject.login(token);)*/
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken)
            throws AuthenticationException {
        // 获取登录用户名
        String sellerId = authenticationToken.getPrincipal().toString();
        System.out.println("sellerId = " + sellerId);
        System.out.println("sellerService = " + sellerService);

        try {
            Seller seller = sellerService.findOne(sellerId);
            // 判断是否为空，审核通过
            if (seller != null && "1".equals(seller.getStatus())){
                return new SimpleAuthenticationInfo(sellerId, seller.getPassword(),
                        ByteSource.Util.bytes(seller.getSellerId()) , this.getName());
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
}
