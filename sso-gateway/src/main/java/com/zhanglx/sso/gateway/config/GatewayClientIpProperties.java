package com.zhanglx.sso.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关客户端 IP 识别配置属性。
 */
@Data
@Component
@ConfigurationProperties(prefix = "sso.client-ip")
public class GatewayClientIpProperties {
    /**
     * Forwarded-For 请求头名称。
     */
    private String forwardedForHeader = "X-Forwarded-For";
    /**
     * 真实IP请求头名称。
     */
    private String realIpHeader = "X-Real-IP";

    private List<String> trustedProxies = new ArrayList<>(List.of(
            "127.0.0.1/32",
            "::1/128",
            "10.0.0.0/8",
            "172.16.0.0/12",
            "192.168.0.0/16"
    ));
}