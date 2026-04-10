package com.zhanglx.sso.auth.config;

import com.zhanglx.sso.auth.constants.PermissionCacheConstants;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;
import java.util.Map;

/**
 * 权限缓存配置。
 * 显式注册 PermissionTree 缓存，避免 RedisCacheManager 在禁止动态创建缓存名时抛出
 * "Cannot find cache named 'PermissionTree'"，从而影响权限树查询主流程。
 */
@Configuration(proxyBeanMethods = false)
public class PermissionCacheConfig {

    /**
     * 权限树变更频率相对较低，但又不能长期使用过期数据，默认缓存 30 分钟。
     */
    private static final Duration PERMISSION_TREE_TTL = Duration.ofMinutes(30);

    @Bean
    public RedisCacheManagerBuilderCustomizer permissionTreeRedisCacheCustomizer() {
        return builder -> builder.withInitialCacheConfigurations(Map.of(
                PermissionCacheConstants.PERMISSION_TREE_CACHE,
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(PERMISSION_TREE_TTL)
                        .disableCachingNullValues()
        ));
    }

}