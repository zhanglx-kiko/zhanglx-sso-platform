package com.zhanglx.sso.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/26 14:57
 * @ClassName: GatewayConfig
 * @Description: 自定义 IP 限流 KeyResolver
 */
@Configuration
public class GatewayConfig {

    // 按IP限流的KeyResolver
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
        );
    }

}
