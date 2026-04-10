package com.zhanglx.sso.gateway.filter;

import cn.dev33.satoken.same.SaSameUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 作者：Zhang L X
 * 创建时间：2026/3/24
 * 类名：转发鉴权过滤器
 * 说明：全局过滤器：为转发给下游微服务的请求注入 同源令牌
 */
@Component
public class ForwardAuthFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 调用 SaSameUtil.getToken() 生成内部暗号，并放入 Header
        ServerHttpRequest newRequest = exchange.getRequest()
                .mutate()
                .header(SaSameUtil.SAME_TOKEN, SaSameUtil.getToken())
                .build();

        ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();
        return chain.filter(newExchange);
    }

    @Override
    public int getOrder() {
        // 优先级设高一点，确保在路由转发给下游之前执行即可
        return -100;
    }

}