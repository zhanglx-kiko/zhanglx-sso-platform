package com.zhanglx.sso.xss.interceptor;

import com.zhanglx.sso.xss.annotation.XssPolicy;
import com.zhanglx.sso.xss.support.XssInputSource;
import com.zhanglx.sso.xss.support.XssPolicyMode;
import com.zhanglx.sso.xss.support.XssPolicyResolution;
import com.zhanglx.sso.xss.support.XssSanitizationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.HashMap;
import java.util.Map;

/**
 * 路径变量清洗拦截器。
 * PathVariable 不会经过 Query/Form 包装器，所以需要单独补这一刀。
 */
@Component
@RequiredArgsConstructor
public class XssPathVariableInterceptor implements HandlerInterceptor {

    private final XssSanitizationService sanitizationService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)
                || sanitizationService.shouldSkipRequest(request)
                || sanitizationService.shouldIgnoreEndpoint(handlerMethod.getBeanType(), handlerMethod.getMethod(), request)) {
            return true;
        }

        Object attribute = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (!(attribute instanceof Map<?, ?> rawMap) || rawMap.isEmpty()) {
            return true;
        }

        Map<String, XssPolicyResolution> configuredPolicies = resolvePathVariablePolicies(handlerMethod, request);
        @SuppressWarnings("unchecked")
        Map<String, String> pathVariables = (Map<String, String>) rawMap;
        Map<String, String> sanitizedPathVariables = new HashMap<>(pathVariables.size());
        pathVariables.forEach((variableName, originalValue) -> {
            XssPolicyResolution resolution = configuredPolicies.getOrDefault(
                    variableName,
                    sanitizationService.resolvePolicy(variableName, null, XssPolicyMode.TEXT, request)
            );
            sanitizedPathVariables.put(
                    variableName,
                    sanitizationService.sanitize(
                            originalValue,
                            variableName,
                            resolution.mode(),
                            XssInputSource.PATH_VARIABLE,
                            request
                    ).value()
            );
        });
        // Spring 在不同版本和不同测试链路下可能返回不可变 Map，这里复制后回写可以避免直接修改原属性失败。
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, sanitizedPathVariables);
        return true;
    }

    private Map<String, XssPolicyResolution> resolvePathVariablePolicies(HandlerMethod handlerMethod,
                                                                         HttpServletRequest request) {
        Map<String, XssPolicyResolution> policies = new HashMap<>();
        for (MethodParameter methodParameter : handlerMethod.getMethodParameters()) {
            PathVariable pathVariable = methodParameter.getParameterAnnotation(PathVariable.class);
            if (pathVariable == null) {
                continue;
            }
            String variableName = resolvePathVariableName(pathVariable, methodParameter);
            if (variableName == null) {
                continue;
            }
            XssPolicy annotation = methodParameter.getParameterAnnotation(XssPolicy.class);
            if (annotation != null) {
                policies.put(
                        variableName,
                        sanitizationService.resolvePolicy(
                                variableName,
                                methodParameter.getParameter(),
                                annotation.value(),
                                request
                        )
                );
            }
        }
        return policies;
    }

    private String resolvePathVariableName(PathVariable pathVariable, MethodParameter methodParameter) {
        if (!pathVariable.name().isBlank()) {
            return pathVariable.name();
        }
        if (!pathVariable.value().isBlank()) {
            return pathVariable.value();
        }
        return methodParameter.getParameterName();
    }
}
