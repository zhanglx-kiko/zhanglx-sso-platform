package com.zhanglx.sso.gateway.filter;

import com.zhanglx.sso.common.ResultCode;
import com.zhanglx.sso.common.result.Result;
import lombok.RequiredArgsConstructor;
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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 统一响应包装过滤器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResponseWrapperFilter implements GlobalFilter, Ordered {
    /**
     * 对象映射器。
     */
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse originalResponse = exchange.getResponse();
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (getStatusCode() == null || !getStatusCode().isError()) {
                    return super.writeWith(body);
                }
                return Flux.from(body)
                        .collectList()
                        .flatMap(dataBuffers -> rewriteErrorBody(getDelegate(), currentStatusCode(), dataBuffers));
            }

            @Override
            public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
                return writeWith(Flux.from(body).flatMapSequential(publisher -> publisher));
            }

            @Override
            public Mono<Void> setComplete() {
                if (getStatusCode() == null || !getStatusCode().isError() || isCommitted()) {
                    return super.setComplete();
                }
                return writeNormalizedError(getDelegate(), currentStatusCode(), extractErrorMessage(null, currentStatusCode()));
            }

            /**
             * 当前状态码处理逻辑。
             */
            private int currentStatusCode() {
                return getStatusCode() == null ? 500 : getStatusCode().value();
            }
        };

        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

    /**
     * rewriteErrorBody处理逻辑。
     */
    private Mono<Void> rewriteErrorBody(ServerHttpResponse targetResponse, int statusCode, List<? extends DataBuffer> dataBuffers) {
        byte[] originalBytes = readBodyBytes(dataBuffers);
        String originalBody = new String(originalBytes, StandardCharsets.UTF_8);
        if (isStandardResultBody(originalBody)) {
            targetResponse.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return writeBytes(targetResponse, originalBytes);
        }
        return writeNormalizedError(targetResponse, statusCode, extractErrorMessage(originalBody, statusCode));
    }

    /**
     * 写出normalizedError。
     */
    private Mono<Void> writeNormalizedError(ServerHttpResponse targetResponse, int statusCode, String message) {
        Result<Void> normalized = Result.error(statusCode, message);
        try {
            byte[] normalizedBytes = objectMapper.writeValueAsBytes(normalized);
            targetResponse.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return writeBytes(targetResponse, normalizedBytes);
        } catch (Exception e) {
            log.error("gateway normalized error response build failed", e);
            return targetResponse.setComplete();
        }
    }

    /**
     * 读取请求体字节数组。
     */
    private byte[] readBodyBytes(List<? extends DataBuffer> dataBuffers) {
        int totalLength = dataBuffers.stream().mapToInt(DataBuffer::readableByteCount).sum();
        byte[] content = new byte[totalLength];
        int offset = 0;
        for (DataBuffer dataBuffer : dataBuffers) {
            int readable = dataBuffer.readableByteCount();
            dataBuffer.read(content, offset, readable);
            offset += readable;
            DataBufferUtils.release(dataBuffer);
        }
        return content;
    }

    /**
     * 写出bytes。
     */
    private Mono<Void> writeBytes(ServerHttpResponse targetResponse, byte[] bytes) {
        DataBuffer buffer = targetResponse.bufferFactory().wrap(bytes);
        return targetResponse.writeWith(Mono.just(buffer));
    }

    /**
     * 判断是否为标准返回体。
     */
    private boolean isStandardResultBody(String body) {
        if (body == null || body.isBlank()) {
            return false;
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(body);
            return jsonNode.isObject() && jsonNode.has("code") && jsonNode.has("msg") && jsonNode.has("data");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * extractErrorMessage处理逻辑。
     */
    private String extractErrorMessage(String body, int statusCode) {
        if (body != null && !body.isBlank()) {
            try {
                JsonNode jsonNode = objectMapper.readTree(body);
                if (jsonNode.hasNonNull("msg")) {
                    return jsonNode.get("msg").asText();
                }
                if (jsonNode.hasNonNull("message")) {
                    return jsonNode.get("message").asText();
                }
                if (jsonNode.hasNonNull("error")) {
                    return jsonNode.get("error").asText();
                }
                if (jsonNode.hasNonNull("detail")) {
                    return jsonNode.get("detail").asText();
                }
            } catch (Exception e) {
                log.debug("gateway error body is not json, fallback to status message");
            }
        }

        return switch (statusCode) {
            case 400 -> ResultCode.BAD_REQUEST.getMessage();
            case 401 -> ResultCode.UNAUTHORIZED.getMessage();
            case 403 -> ResultCode.FORBIDDEN.getMessage();
            case 404 -> ResultCode.NOT_FOUND.getMessage();
            case 405 -> ResultCode.METHOD_NOT_ALLOWED.getMessage();
            case 409 -> ResultCode.CONFLICT.getMessage();
            case 422 -> ResultCode.UNPROCESSABLE_ENTITY.getMessage();
            case 429 -> ResultCode.TOO_MANY_REQUESTS.getMessage();
            case 502 -> ResultCode.BAD_GATEWAY.getMessage();
            case 503 -> ResultCode.SERVICE_UNAVAILABLE.getMessage();
            case 504 -> ResultCode.GATEWAY_TIMEOUT.getMessage();
            default -> ResultCode.INTERNAL_SERVER_ERROR.getMessage();
        };
    }

    @Override
    public int getOrder() {
        return -1;
    }
}