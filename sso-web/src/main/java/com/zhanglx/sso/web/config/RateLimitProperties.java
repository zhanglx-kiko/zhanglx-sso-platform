package com.zhanglx.sso.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * RateLimit配置属性。
 */
@Data
@Component
@ConfigurationProperties(prefix = "sso.rate-limit")
public class RateLimitProperties {
    /**
     * 是否启用。
     */
    private boolean enabled = true;
    /**
     * defaultLimit。
     */
    private long defaultLimit = 60L;
    /**
     * 默认时间窗口秒数。
     */
    private long defaultWindowSeconds = 60L;
    /**
     * 键前缀。
     */
    private String keyPrefix = "sso:rate-limit";
    /**
     * 默认消息键。
     */
    private String defaultMessageKey = "request.rate.limit.exceeded";
    /**
     * 是否启用本地兜底。
     */
    private boolean localFallbackEnabled = true;
    /**
     * writeResponseHeaders。
     */
    private boolean writeResponseHeaders = true;
    /**
     * whitelist地址s。
     */
    private List<String> whitelistIps = new ArrayList<>();
    /**
     * 白名单路径集合。
     */
    private List<String> whitelistPaths = new ArrayList<>();
}