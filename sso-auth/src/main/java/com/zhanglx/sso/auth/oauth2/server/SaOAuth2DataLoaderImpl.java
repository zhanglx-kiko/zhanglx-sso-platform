package com.zhanglx.sso.auth.oauth2.server;

import cn.dev33.satoken.oauth2.consts.GrantType;
import cn.dev33.satoken.oauth2.data.loader.SaOAuth2DataLoader;
import cn.dev33.satoken.oauth2.data.model.loader.SaClientModel;
import org.springframework.stereotype.Component;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/11 15:33
 * @ClassName: SaOAuth2DataLoaderImpl
 * @Description: 自定义数据加载器
 */
@Component
public class SaOAuth2DataLoaderImpl implements SaOAuth2DataLoader {

    // 根据 clientId 获取 Client 信息
    @Override
    public SaClientModel getClientModel(String clientId) {
        // 此为模拟数据，真实环境需要从数据库查询
        if("1001".equals(clientId)) {
            return new SaClientModel()
                    // 客户端 id
                    .setClientId("1001")
                    // 客户端秘钥
                    .setClientSecret("aaaa-bbbb-cccc-dddd-eeee")
                    // 允许重定向的授权回调地址 (生产环境切忌用 *, 必须精确匹配 配置到详细地址)
                    .addAllowRedirectUris("*")
                    // 该客户端允许申请的权限范围
                    .addContractScopes("openid", "userid", "userinfo")
                    .addAllowGrantTypes(     // 所有允许的授权模式
                            GrantType.authorization_code, // 授权码式
                            GrantType.implicit,  // 隐式式
                            GrantType.refresh_token,  // 刷新令牌
                            GrantType.password,  // 密码式
                            GrantType.client_credentials,  // 客户端模式
                            "phone_code"  // 自定义授权模式 手机号验证码登录
                    )
                    .setIsNewRefresh(true);
        }
        return null;
    }

    // 根据 clientId 和 loginId 获取 openid
    @Override
    public String getOpenid(String clientId, Object loginId) {
        // 此处使用框架默认算法生成 openid，真实环境建议改为从数据库查询
        // 最简单的实现：直接返回 userId。
        // 安全要求高的实现：针对不同的 clientId 和 userId，生成一个不可逆的加密字符串作为 openid
        return SaOAuth2DataLoader.super.getOpenid(clientId, loginId);
    }

}
