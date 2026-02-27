package com.zhanglx.sso.core;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/26 15:46
 * @ClassName: CoreAutoConfig
 * @Description:
 */
@Configuration
@ComponentScan(basePackages = "com.zhanglx.sso.core")
public class CoreAutoConfig {
}
