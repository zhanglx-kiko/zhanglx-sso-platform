package com.zhanglx.sso.gateway.filter;

import com.zhanglx.sso.common.net.ClientIpUtils;
import com.zhanglx.sso.common.trace.TraceConstants;
import com.zhanglx.sso.gateway.config.GatewayClientIpProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

/**
 * 网关全局请求日志过滤器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayGlobalLogFilter implements GlobalFilter, Ordered {
    /**
     * clientIpProperties。
     */
    private final GatewayClientIpProperties clientIpProperties;

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = resolveRequestId(exchange.getRequest());
        String traceId = resolveTraceId(exchange.getRequest(), requestId);
        String clientIp = resolveClientIp(exchange.getRequest());
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header(TraceConstants.REQUEST_ID_HEADER, requestId)
                .header(TraceConstants.TRACE_ID_HEADER, traceId)
                .header("X-Client-Ip", clientIp)
                .build();

        ServerWebExchange modifiedExchange = exchange.mutate().request(request).build();
        String method = request.getMethod() == null ? "UNKNOWN" : request.getMethod().name();
        String path = request.getPath().value();
        String query = request.getURI().getRawQuery();
        long startTime = System.currentTimeMillis();

        return chain.filter(modifiedExchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = modifiedExchange.getResponse();
            int statusCode = Optional.ofNullable(response.getStatusCode()).orElse(HttpStatusCode.valueOf(500)).value();
            long costTime = System.currentTimeMillis() - startTime;
            log.info("gateway_access traceId={} requestId={} clientIp={} method={} path={} query={} status={} costMs={}",
                    traceId,
                    requestId,
                    clientIp,
                    method,
                    path,
                    StringUtils.hasText(query) ? query : "",
                    statusCode,
                    costTime);
        }));
    }

    /**
     * 解析请求标识。
     */
    private String resolveRequestId(ServerHttpRequest request) {
        String requestId = request.getHeaders().getFirst(TraceConstants.REQUEST_ID_HEADER);
        return StringUtils.hasText(requestId) ? requestId.trim() : UUID.randomUUID().toString();
    }

    /**
     * 解析链路追踪标识。
     */
    private String resolveTraceId(ServerHttpRequest request, String requestId) {
        String traceId = request.getHeaders().getFirst(TraceConstants.TRACE_ID_HEADER);
        return StringUtils.hasText(traceId) ? traceId.trim() : requestId;
    }

    /**
     * 解析客户端地址。
     */
    private String resolveClientIp(ServerHttpRequest request) {
        String remoteAddress = request.getRemoteAddress() == null || request.getRemoteAddress().getAddress() == null
                ? null
                : request.getRemoteAddress().getAddress().getHostAddress();
        return ClientIpUtils.resolveClientIp(
                remoteAddress,
                request.getHeaders().getFirst(clientIpProperties.getForwardedForHeader()),
                request.getHeaders().getFirst(clientIpProperties.getRealIpHeader()),
                clientIpProperties.getTrustedProxies()
        );
    }
}