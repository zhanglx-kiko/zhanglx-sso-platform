package com.zhanglx.sso.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/26 14:23
 * @ClassName: ResponseWrapperFilter
 * @Description: 网关级别的响应过滤器
 */
@Slf4j
@Component
public class ResponseWrapperFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse originalResponse = exchange.getResponse();

        log.info("ResponseWrapperFilter 开始处理请求: {} {}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getPath());

        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                log.info("ResponseWrapperFilter writeWith 被调用, 状态码: {}", getStatusCode());

                if (getStatusCode() != null && getStatusCode().isError()) {
                    log.info("检测到错误状态码: {}, 开始处理错误响应", getStatusCode().value());

                    return Flux.from(body)
                            .collectList()
                            .flatMap(dataBuffers -> {
                                // 收集响应内容
                                StringBuilder responseBody = new StringBuilder();
                                dataBuffers.forEach(dataBuffer -> {
                                    byte[] content = new byte[dataBuffer.readableByteCount()];
                                    dataBuffer.read(content);
                                    DataBufferUtils.release(dataBuffer);
                                    responseBody.append(new String(content, StandardCharsets.UTF_8));
                                });

                                String originalResponseStr = responseBody.toString();
                                log.info("原始错误响应内容: {}", originalResponseStr);

                                try {
                                    // 尝试解析原始错误信息
                                    String errorMessage = extractErrorMessage(originalResponseStr);
                                    log.info("解析出的错误信息: {}", errorMessage);

                                    // 构造统一的错误响应格式
                                    Map<String, Object> errorResponse = new HashMap<>();
                                    errorResponse.put("code", getStatusCode().value());
                                    errorResponse.put("msg", errorMessage);
                                    errorResponse.put("data", null);
                                    errorResponse.put("path", exchange.getRequest().getPath().value());
                                    errorResponse.put("timestamp", System.currentTimeMillis());

                                    String jsonResponse = new ObjectMapper().writeValueAsString(errorResponse);
                                    log.info("构造的统一错误响应: {}", jsonResponse);

                                    DataBuffer buffer = exchange.getResponse().bufferFactory()
                                            .wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));

                                    // 设置响应头
                                    exchange.getResponse().setStatusCode(getStatusCode());
                                    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

                                    return super.writeWith(Mono.just(buffer));
                                } catch (Exception e) {
                                    log.error("解析或构造错误响应失败", e);
                                    // 如果解析失败，返回原始响应
                                    return super.writeWith(Flux.fromIterable(dataBuffers));
                                }
                            });
                } else {
                    log.info("非错误状态码: {}, 直接返回原始响应", getStatusCode());
                }
                return super.writeWith(body);
            }
        };

        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

    /**
     * 解析错误信息
     */
    private String extractErrorMessage(String responseBody) {
        try {
            // 解析 JSON 格式的错误响应
            if (responseBody.startsWith("{") && responseBody.endsWith("}")) {
                Map errorMap = new ObjectMapper().readValue(responseBody, Map.class);
                if (errorMap.containsKey("msg")) {
                    return errorMap.get("msg").toString();
                } else if (errorMap.containsKey("message")) {
                    return errorMap.get("message").toString();
                }
            }
            // 如果解析失败，返回默认信息
            return "系统内部错误";
        } catch (Exception e) {
            log.warn("解析错误信息失败: {}", responseBody);
            return "系统内部错误";
        }
    }


    @Override
    public int getOrder() {
        return -1; // 在日志过滤器之后执行
    }

}
