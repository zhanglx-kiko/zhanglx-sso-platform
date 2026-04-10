package com.zhanglx.sso.log;

import com.zhanglx.sso.log.config.OperationLogProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 操作日志自动装配。
 * 只有显式开启操作日志时才扫描整个日志模块，避免在关闭场景下继续初始化 ES 客户端、
 * 异步投递器等组件，导致无意义的资源占用或连接失败。
 */
@Configuration
@ComponentScan(basePackages = "com.zhanglx.sso.log")
@EnableConfigurationProperties(OperationLogProperties.class)
public class LogAutoConfig {
}