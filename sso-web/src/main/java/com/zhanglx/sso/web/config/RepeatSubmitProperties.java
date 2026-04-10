package com.zhanglx.sso.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 重复提交配置属性。
 */
@Data
@Component
@ConfigurationProperties(prefix = "sso.repeat-submit")
public class RepeatSubmitProperties {
    /**
     * 是否启用。
     */
    private boolean enabled = true;
    /**
     * 默认时间窗口秒数。
     */
    private long defaultWindowSeconds = 5L;
    /**
     * 键前缀。
     */
    private String keyPrefix = "sso:repeat-submit";
    /**
     * 默认消息键。
     */
    private String defaultMessageKey = "repeat.submit";
    /**
     * 是否启用本地兜底。
     */
    private boolean localFallbackEnabled = true;
}