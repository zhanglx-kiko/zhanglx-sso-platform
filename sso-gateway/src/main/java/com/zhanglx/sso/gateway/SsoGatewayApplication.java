package com.zhanglx.sso.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 网关启动入口。
 */
@SpringBootApplication
public class SsoGatewayApplication {

    static void main(String[] args) {
        SpringApplication.run(SsoGatewayApplication.class, args);
    }

}