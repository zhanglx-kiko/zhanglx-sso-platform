package com.zhanglx.sso.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "sso.repeat-submit")
public class RepeatSubmitProperties {

    private boolean enabled = true;

    private long defaultWindowSeconds = 5L;

    private String keyPrefix = "sso:repeat-submit";

    private String defaultMessageKey = "repeat.submit";

    private boolean localFallbackEnabled = true;
}
