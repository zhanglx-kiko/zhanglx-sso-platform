package com.zhanglx.sso.horticulturalplants;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableCaching
@EnableFeignClients(basePackages = "com.zhanglx.sso.horticulturalplants.remote")
@SpringBootApplication
public class HorticulturalPlantsApplication {

    public static void main(String[] args) {
        SpringApplication.run(HorticulturalPlantsApplication.class, args);
    }
}
