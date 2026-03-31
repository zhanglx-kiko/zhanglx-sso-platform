package com.zhanglx.sso.core.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.same.SaSameUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/24 15:28
 * @ClassName: SaTokenWebMvcConfig
 * @Description: 下游微服务全局拦截器配置
 */
@Configuration
public class SaTokenWebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 的路由拦截器
        registry.addInterceptor(new SaInterceptor(handle -> {
                    // 正确的方法：校验当前 Request 上下文提供的 Same-Token 是否有效
                    // 如果无效（如绕过网关直接访问内网IP），内部会直接抛出 SameTokenInvalidException 异常
                    SaSameUtil.checkCurrentRequestToken();

                })).addPathPatterns("/**")
                // 强烈建议排除微服务的健康检查和监控接口，否则网关或注册中心无法拉取服务状态
                .excludePathPatterns("/actuator/**", "/v3/api-docs/**");
    }

}
