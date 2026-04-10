package com.zhanglx.sso.xss;

import com.zhanglx.sso.xss.config.XssProtectionProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * XSS 安全组件自动装配。
 * 这里只在 Servlet Web 应用中生效，避免网关这类 响应式 Web 应用误装配。
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.zhanglx.sso.xss")
@EnableConfigurationProperties(XssProtectionProperties.class)
@ConditionalOnClass({HttpServletRequest.class, DispatcherServlet.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "sso.xss", name = "enabled", havingValue = "true")
public class XssAutoConfig {
}