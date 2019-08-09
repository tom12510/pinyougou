package com.pinyougou.user.realm;

import io.buji.pac4j.realm.Pac4jRealm;
import io.buji.pac4j.subject.Pac4jPrincipal;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;

/**
 * 自定义认证域
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-01<p>
 */
public class CasPac4jRealm extends Pac4jRealm{

    /** 身份认证 */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
            throws AuthenticationException {
        AuthenticationInfo authenticationInfo =  super.doGetAuthenticationInfo(token);
        Pac4jPrincipal principal = (Pac4jPrincipal) authenticationInfo.getPrincipals()
                .getPrimaryPrincipal();
        System.out.println("登录用户名：" + principal.getName());
        return authenticationInfo;
    }

    /** 授权 */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

        Pac4jPrincipal principal = (Pac4jPrincipal) principals.getPrimaryPrincipal();
        System.out.println("登录用户名：" + principal.getName());
        // 角色与权限
        return super.doGetAuthorizationInfo(principals);
    }

}
