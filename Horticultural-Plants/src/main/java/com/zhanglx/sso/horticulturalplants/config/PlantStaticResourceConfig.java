package com.zhanglx.sso.horticulturalplants.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class PlantStaticResourceConfig implements WebMvcConfigurer {

    private static final String DEFAULT_CLASSPATH_LOCATION = "classpath:/plant-seeds/";

    private final PlantStorageProperties plantStorageProperties;

    public PlantStaticResourceConfig(PlantStorageProperties plantStorageProperties) {
        this.plantStorageProperties = plantStorageProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String publicUrlPrefix = normalizePublicUrlPrefix(plantStorageProperties.getPublicUrlPrefix());
        Path basePath = resolveBasePath(plantStorageProperties.getBasePath());
        String fileLocation = basePath.toUri().toString();
        if (!fileLocation.endsWith("/")) {
            fileLocation = fileLocation + "/";
        }
        registry.addResourceHandler(publicUrlPrefix + "**")
                .addResourceLocations(fileLocation, DEFAULT_CLASSPATH_LOCATION);
    }

    private String normalizePublicUrlPrefix(String publicUrlPrefix) {
        String resolved = StringUtils.hasText(publicUrlPrefix)
                ? publicUrlPrefix.trim()
                : "/apis/v1/horticultural-plants/public/assets/";
        if (!resolved.endsWith("/")) {
            resolved = resolved + "/";
        }
        return resolved;
    }

    private Path resolveBasePath(String basePath) {
        String resolved = StringUtils.hasText(basePath)
                ? basePath.trim()
                : "./storage/member-plant-assets/";
        return Paths.get(resolved).toAbsolutePath().normalize();
    }
}
