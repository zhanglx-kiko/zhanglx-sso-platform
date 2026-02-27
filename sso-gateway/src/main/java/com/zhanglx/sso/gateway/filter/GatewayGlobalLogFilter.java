package com.zhanglx.sso.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/12 15:28
 * @ClassName: GatewayGlobalLogFilter
 * @Description: 网关全局日志过滤器 记录请求IP、方法、路径、参数、响应状态、耗时等核心信息
 */
@Slf4j
@Component
public class GatewayGlobalLogFilter implements GlobalFilter, Ordered {

    // 过滤器优先级（4.2.7建议设为HIGHEST_PRECEDENCE+10，优先于其他过滤器执行）
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 生成请求ID并添加到请求头
        String requestId = UUID.randomUUID().toString();
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header("X-Request-Id", requestId)
                .build();

        ServerWebExchange modifiedExchange = exchange.mutate().request(request).build();

        // 2. 获取请求核心信息
        String method = request.getMethod().name();
        String path = request.getPath().value();
        String query = request.getQueryParams().toString();
        String ip = getClientRealIp(request);

        // 3. 记录请求开始时间
        long startTime = System.currentTimeMillis();

        // 4. 执行后续过滤器链，完成后记录响应日志
        return chain.filter(modifiedExchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = modifiedExchange.getResponse();
            int statusCode = Optional.ofNullable(response.getStatusCode()).orElse(HttpStatusCode.valueOf(500)).value();
            long costTime = System.currentTimeMillis() - startTime;

            // 5. 输出结构化日志
            log.info("Gateway Log | requestId: {}, ip: {}, method: {}, path: {}, query: {}, status: {}, cost: {}ms",
                    requestId, ip, method, path, query, statusCode, costTime);
        }));
    }

    /**
     * 获取客户端真实IP（适配4.2.7版本网关转发场景）
     */
    private String getClientRealIp(ServerHttpRequest request) {
        return Optional.ofNullable(request.getHeaders().getFirst("X-Forwarded-For"))
                .orElseGet(() -> Optional.ofNullable(request.getHeaders().getFirst("X-Real-IP"))
                        .orElseGet(() -> {
                            InetSocketAddress remoteAddress = request.getRemoteAddress();
                            return remoteAddress != null ? remoteAddress.getHostString() : "unknown";
                        }));
    }

}
