package com.zhanglx.sso.horticulturalplants.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "horticultural.plants.storage")
public class PlantStorageProperties {

    private String basePath;

    private String publicUrlPrefix;
}
