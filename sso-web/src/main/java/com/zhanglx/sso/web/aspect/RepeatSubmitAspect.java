package com.zhanglx.sso.web.aspect;

import com.zhanglx.sso.common.ResultCode;
import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.web.annotation.RepeatSubmit;
import com.zhanglx.sso.web.config.RepeatSubmitProperties;
import com.zhanglx.sso.web.exception.WebRequestProtectionErrorCode;
import com.zhanglx.sso.web.support.RequestIdentityAccessor;
import com.zhanglx.sso.web.support.RequestProtectionStore;
import com.zhanglx.sso.web.support.WebExpressionEvaluator;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
@RequiredArgsConstructor
public class RepeatSubmitAspect {

    private final ObjectMapper objectMapper;
    private final RepeatSubmitProperties properties;
    private final RequestProtectionStore requestProtectionStore;
    private final WebExpressionEvaluator webExpressionEvaluator;
    private final RequestIdentityAccessor requestIdentityAccessor;

    @Around("@annotation(repeatSubmit)")
    public Object around(ProceedingJoinPoint joinPoint, RepeatSubmit repeatSubmit) throws Throwable {
        if (!properties.isEnabled()) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = currentRequest();
        if (request == null) {
            return joinPoint.proceed();
        }
        if (!webExpressionEvaluator.matchesCondition(repeatSubmit.condition(), joinPoint, request)) {
            return joinPoint.proceed();
        }

        Duration window = resolveWindow(repeatSubmit);
        String lockKey = buildLockKey(joinPoint, request, repeatSubmit);
        boolean acquired = requestProtectionStore.tryAcquireRepeatSubmit(
                lockKey,
                window,
                properties.isLocalFallbackEnabled()
        );
        if (!acquired) {
            log.warn("repeat submit blocked, uri={}, actor={}, key={}",
                    request.getRequestURI(),
                    requestIdentityAccessor.resolveActorKey(request),
                    lockKey);
            throw repeatSubmitException(repeatSubmit);
        }
        return joinPoint.proceed();
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    private Duration resolveWindow(RepeatSubmit repeatSubmit) {
        long seconds = repeatSubmit.windowSeconds() > 0L
                ? repeatSubmit.windowSeconds()
                : properties.getDefaultWindowSeconds();
        return Duration.ofSeconds(Math.max(1L, seconds));
    }

    private String buildLockKey(ProceedingJoinPoint joinPoint, HttpServletRequest request, RepeatSubmit repeatSubmit) {
        String customKey = webExpressionEvaluator.evaluateAsString(repeatSubmit.key(), joinPoint, request);
        String payload = StringUtils.hasText(customKey) ? customKey : buildCanonicalPayload(joinPoint);
        String fingerprint = DigestUtils.md5DigestAsHex(payload.getBytes(StandardCharsets.UTF_8));
        return String.join(":",
                properties.getKeyPrefix(),
                request.getMethod(),
                request.getRequestURI(),
                requestIdentityAccessor.resolveActorKey(request),
                fingerprint);
    }

    private String buildCanonicalPayload(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] arguments = joinPoint.getArgs();

        Map<String, Object> payload = new LinkedHashMap<>();
        for (int i = 0; i < arguments.length; i++) {
            Object argument = arguments[i];
            if (!shouldIncludeArgument(argument)) {
                continue;
            }
            String parameterName = parameterNames != null && i < parameterNames.length
                    ? parameterNames[i]
                    : "arg" + i;
            payload.put(parameterName, argument);
        }

        JsonNode canonicalNode = canonicalize(objectMapper.valueToTree(payload));
        try {
            return objectMapper.writeValueAsString(canonicalNode);
        } catch (Exception ex) {
            log.debug("serialize repeat submit payload failed, fallback to node text", ex);
            return canonicalNode.toString();
        }
    }

    private JsonNode canonicalize(JsonNode node) {
        if (node == null || node.isNull() || node.isValueNode()) {
            return node;
        }
        if (node.isObject()) {
            ObjectNode objectNode = objectMapper.createObjectNode();
            node.propertyNames().stream()
                    .sorted()
                    .forEach(fieldName -> objectNode.set(fieldName, canonicalize(node.get(fieldName))));
            return objectNode;
        }
        if (node.isArray()) {
            ArrayNode arrayNode = objectMapper.createArrayNode();
            node.forEach(item -> arrayNode.add(canonicalize(item)));
            return arrayNode;
        }
        return node;
    }

    private boolean shouldIncludeArgument(Object argument) {
        if (argument == null) {
            return true;
        }
        return !(argument instanceof ServletRequest
                || argument instanceof ServletResponse
                || argument instanceof MultipartFile
                || argument instanceof MultipartFile[]
                || argument instanceof BindingResult
                || argument instanceof Principal
                || argument instanceof Reader
                || argument instanceof Writer
                || argument instanceof InputStream
                || argument instanceof OutputStream
                || argument instanceof Locale);
    }

    private BusinessException repeatSubmitException(RepeatSubmit repeatSubmit) {
        if (StringUtils.hasText(repeatSubmit.messageKey())) {
            return new BusinessException(ResultCode.TOO_MANY_REQUESTS.getCode(), repeatSubmit.messageKey().trim());
        }
        if (StringUtils.hasText(repeatSubmit.message())) {
            return new BusinessException(ResultCode.TOO_MANY_REQUESTS.getCode(), repeatSubmit.message().trim());
        }
        if (StringUtils.hasText(properties.getDefaultMessageKey())) {
            return new BusinessException(ResultCode.TOO_MANY_REQUESTS.getCode(), properties.getDefaultMessageKey().trim());
        }
        return new BusinessException(WebRequestProtectionErrorCode.REPEAT_SUBMIT);
    }
}
