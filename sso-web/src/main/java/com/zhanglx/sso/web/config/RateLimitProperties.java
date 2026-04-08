package com.zhanglx.sso.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "sso.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;

    private long defaultLimit = 60L;

    private long defaultWindowSeconds = 60L;

    private String keyPrefix = "sso:rate-limit";

    private String defaultMessageKey = "request.rate.limit.exceeded";

    private boolean localFallbackEnabled = true;

    private boolean writeResponseHeaders = true;

    private List<String> whitelistIps = new ArrayList<>();

    private List<String> whitelistPaths = new ArrayList<>();
}
