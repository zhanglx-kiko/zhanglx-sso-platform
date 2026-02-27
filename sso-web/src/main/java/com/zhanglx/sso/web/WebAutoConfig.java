package com.zhanglx.sso.web;

import com.fasterxml.jackson.databind.ObjectMapper;
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
@ComponentScan(basePackages = "com.zhanglx.sso.web")
public class WebAutoConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
