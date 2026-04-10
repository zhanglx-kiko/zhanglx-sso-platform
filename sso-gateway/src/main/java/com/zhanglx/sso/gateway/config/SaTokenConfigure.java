package com.zhanglx.sso.gateway.config;

import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 作者：Zhang L X
 * 创建时间：2026/2/10 20:22
 * 类名：SaTokenConfigure
 * 说明：网关全局鉴权配置
 */
@Configuration
public class SaTokenConfigure {

    private static final StpLogic MEMBER_STP_LOGIC = new StpLogic("member");

    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                // 拦截地址
                .addInclude("/**")
                // 开放地址 (登录接口、静态资源等不需要鉴权)
                .addExclude("/favicon.ico",
                        "/apis/v1/auth/s/login",
                        "/apis/v1/auth/s/forgot-password",
                        "/apis/v1/auth/m/login",
                        "/apis/v1/auth/m/register",
                        "/apis/v1/auth/m/forgot-password",
                        "/apis/v1/auth/m/verification-code/send",
                        "/apis/v1/auth/m/wechat/login",
                        "/apis/v1/auth/isLogin",
                        "/oauth2/*",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/webjars/**",
                        "/doc.html")
                // 鉴权方法：每次请求都会执行
                .setAuth(obj -> {
                    // 登录校验 -- 拦截所有路由，并排除 /auth/doLogin 用于开放登录
                    SaRouter.match("/apis/v1/auth/m/**", r -> MEMBER_STP_LOGIC.checkLogin());
                    SaRouter.match("/apis/v1/user/m/**", r -> MEMBER_STP_LOGIC.checkLogin());
                    SaRouter.match("/**")
                            .notMatch("/apis/v1/auth/m/**", "/apis/v1/user/m/**")
                            .check(r -> StpUtil.checkLogin());
                })
                // 异常处理方法：每次 setAuth 函数出现异常时进入
                .setError(e -> {
                    // 返回 JSON 格式的错误信息
                    return SaResult.error(e.getMessage());
                });
    }

}