package com.zhanglx.sso.gateway.config;

import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/10 20:22
 * @ClassName: SaTokenConfigure
 * @Description: 网关全局鉴权配置
 */
@Configuration
public class SaTokenConfigure {

    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                // 拦截地址
                .addInclude("/**")
                // 开放地址 (登录接口、静态资源等不需要鉴权)
                .addExclude("/favicon.ico", "/auth/login", "/auth/register", "/auth/isLogin")
                // 鉴权方法：每次请求都会执行
                .setAuth(obj -> {
                    // 登录校验 -- 拦截所有路由，并排除 /auth/doLogin 用于开放登录
                    SaRouter.match("/**", "/auth/login", r -> StpUtil.checkLogin());
                    SaRouter.match("/**", "/auth/register", r -> StpUtil.checkLogin()); // 登录校验 -- 拦截所有路由，并排除 /auth/doLogin 用于开放登录
                    // 权限认证 -- 不同模块, 校验不同权限
                    // SaRouter.match("/user/**", r -> StpUtil.checkPermission("user"));
                    // SaRouter.match("/admin/**", r -> StpUtil.checkPermission("admin"));
                })
                // 异常处理方法：每次 setAuth 函数出现异常时进入
                .setError(e -> {
                    // 返回 JSON 格式的错误信息
                    return SaResult.error(e.getMessage());
                });
    }

}
