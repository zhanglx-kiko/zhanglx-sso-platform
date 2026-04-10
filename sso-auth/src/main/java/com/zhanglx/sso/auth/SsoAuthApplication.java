package com.zhanglx.sso.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

// @EnableFeignClients(basePackages = "com.zhanglx.sso.api")
@EnableCaching // 开启 Spring 缓存抽象支持
@SpringBootApplication
// @MapperScan("com.zhanglx.sso.auth.mapper")
public class SsoAuthApplication {

    static void main(String[] args) {
        SpringApplication.run(SsoAuthApplication.class, args);
    }
}