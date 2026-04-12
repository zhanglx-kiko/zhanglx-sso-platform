package com.zhanglx.sso.horticulturalplants;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class HorticulturalPlantsApplication {

    public static void main(String[] args) {
        SpringApplication.run(HorticulturalPlantsApplication.class, args);
    }
}
