package com.zhanglx.sso.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "sso.client-ip")
public class ClientIpProperties {

    private String forwardedForHeader = "X-Forwarded-For";

    private String realIpHeader = "X-Real-IP";

    private String tenantHeader = "X-Tenant-Id";

    private List<String> trustedProxies = new ArrayList<>(List.of("127.0.0.1/32", "::1/128"));
}
