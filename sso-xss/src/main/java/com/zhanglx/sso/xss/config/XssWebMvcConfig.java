package com.zhanglx.sso.xss.config;

import com.zhanglx.sso.xss.interceptor.XssPathVariableInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册 XSS 相关 MVC 组件。
 */
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class XssWebMvcConfig implements WebMvcConfigurer {

    private final XssPathVariableInterceptor xssPathVariableInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(xssPathVariableInterceptor).addPathPatterns("/**");
    }
}
