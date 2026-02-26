package com.zhanglx.sso.core.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhanglx.sso.common.utils.Result;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
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

    @Autowired
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
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {

        // 1. 如果返回是 String 类型，需要特殊处理 (这是最容易踩坑的地方)
        if (body instanceof String) {
            // 手动序列化为 JSON 字符串，否则 Spring 会报 ClassCastException
            return objectMapper.writeValueAsString(Result.success(body));
        }

        // 2. 如果已经是 Result 类型，则不再重复包装
        if (body instanceof Result) {
            return body;
        }

        // 3. 这里的 body 就是 Controller 返回的实际数据，我们将它包装成 Result
        return Result.success(body);
    }

}
