package com.zhanglx.sso.web.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhanglx.sso.common.utils.Result;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/11 14:40
 * @ClassName: GlobalResponseHandler
 * @Description: 全局统一响应处理器 自动将 Controller 返回的数据包装为 Result<T> 格式
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.zhanglx")
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    @Resource
    private ObjectMapper objectMapper; // Jackson 工具类

    /**
     * 是否开启拦截
     * return true 表示拦截所有请求
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 可以在这里排除一些不需要包装的接口
        return true;
    }

    /**
     * 响应写出之前的处理逻辑
     */
    @Override
    @SneakyThrows
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        // 检查是否是错误响应
        if (body instanceof Result) {
            Result<?> result = (Result<?>) body;
            if (result.getCode() != null && result.getCode() >= 400) {
                log.info("检测到错误响应，设置相应状态码: {}", result.getCode());
                response.setStatusCode(HttpStatus.valueOf(result.getCode()));
            }
        }

        if (body instanceof String) {
            String responseStr = objectMapper.writeValueAsString(Result.success(body));
            log.info("String 类型响应处理完成: {}", responseStr);
            return responseStr;
        }

        if (body instanceof Result) {
            log.info("Result 类型响应，直接返回: {}", body);
            return body;
        }

        Result<?> successResult = Result.success(body);
        log.info("普通响应包装完成: {}", successResult);
        return successResult;
    }

}
