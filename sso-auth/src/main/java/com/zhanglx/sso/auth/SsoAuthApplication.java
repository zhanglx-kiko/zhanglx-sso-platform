package com.zhanglx.sso.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zhanglx.sso.auth.mapper")
public class SsoAuthApplication {

    public static void main(String[] args) {
        System.out.println("Starting SSO Auth Service with JDK " + System.getProperty("java.version"));
        SpringApplication.run(SsoAuthApplication.class, args);
    }

}
