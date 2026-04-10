package com.zhanglx.sso.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 运行时配置缓存参数。
 * 这里只控制本地内存缓存行为，不承载业务配置本身。
 */
@Data
@Component
@ConfigurationProperties(prefix = "sso.config.runtime")
public class ConfigRuntimeCacheProperties {

    /**
     * 本地缓存过期时间，单位秒。
     * 默认五分钟，兼顾频繁读取性能和多节点最终一致性。
     */
    private long cacheTtlSeconds = 300L;
}
