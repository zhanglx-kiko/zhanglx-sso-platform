package com.zhanglx.sso.web.handler;

import com.zhanglx.sso.common.result.Result;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import tools.jackson.databind.ObjectMapper;

/**
 * 全局响应包装处理器。
 */
@RestControllerAdvice(basePackages = "com.zhanglx")
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    /**
     * 对象映射器。
     */
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    @SneakyThrows
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof Result<?> result) {
            applyHttpStatus(result, response);
            return result;
        }

        if (body == null) {
            return Result.success();
        }

        if (body instanceof CharSequence sequence) {
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return objectMapper.writeValueAsString(Result.success(sequence.toString()));
        }

        return Result.success(body);
    }

    /**
     * 应用响应状态码。
     */
    private void applyHttpStatus(Result<?> result, ServerHttpResponse response) {
        Integer code = result.getCode();
        if (code == null || code < 400) {
            return;
        }

        HttpStatus status = HttpStatus.resolve(code);
        response.setStatusCode(status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR);
    }

}