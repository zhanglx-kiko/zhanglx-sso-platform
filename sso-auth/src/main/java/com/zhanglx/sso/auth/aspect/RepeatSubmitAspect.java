package com.zhanglx.sso.auth.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.zhanglx.sso.auth.annotation.RepeatSubmit;
import com.zhanglx.sso.auth.config.RepeatSubmitProperties;
import com.zhanglx.sso.auth.exception.AuthOperationErrorCode;
import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.core.utils.satoken.StpMemberUtil;
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
import org.springframework.core.io.InputStreamSource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
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
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
@RequiredArgsConstructor
public class RepeatSubmitAspect {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final RepeatSubmitProperties properties;

    private final ConcurrentMap<String, Long> localLockCache = new ConcurrentHashMap<>();

    @Around("@annotation(repeatSubmit)")
    public Object around(ProceedingJoinPoint joinPoint, RepeatSubmit repeatSubmit) throws Throwable {
        if (!properties.isEnabled()) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = currentRequest();
        if (request == null) {
            return joinPoint.proceed();
        }

        Duration window = resolveWindow(repeatSubmit);
        String lockKey = buildLockKey(joinPoint, request);
        if (!tryAcquire(lockKey, request.getRequestURI(), window)) {
            throw new BusinessException(AuthOperationErrorCode.REPEAT_SUBMIT);
        }
        return joinPoint.proceed();
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    private Duration resolveWindow(RepeatSubmit repeatSubmit) {
        long seconds = repeatSubmit.windowSeconds() > 0
                ? repeatSubmit.windowSeconds()
                : properties.getDefaultWindowSeconds();
        return Duration.ofSeconds(Math.max(1L, seconds));
    }

    private boolean tryAcquire(String lockKey, String value, Duration window) {
        try {
            Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, value, window);
            return Boolean.TRUE.equals(success);
        } catch (Exception ex) {
            log.warn("repeat submit redis unavailable, fallback to local cache, key={}", lockKey, ex);
            return tryAcquireLocal(lockKey, window);
        }
    }

    private boolean tryAcquireLocal(String lockKey, Duration window) {
        long now = System.currentTimeMillis();
        long expireAt = now + window.toMillis();
        localLockCache.entrySet().removeIf(entry -> entry.getValue() <= now);
        Long existingExpireAt = localLockCache.putIfAbsent(lockKey, expireAt);
        if (existingExpireAt == null) {
            return true;
        }
        return existingExpireAt <= now && localLockCache.replace(lockKey, existingExpireAt, expireAt);
    }

    private String buildLockKey(ProceedingJoinPoint joinPoint, HttpServletRequest request) {
        String payload = buildCanonicalPayload(joinPoint);
        String fingerprint = DigestUtils.md5DigestAsHex(payload.getBytes(StandardCharsets.UTF_8));
        return String.join(":",
                properties.getKeyPrefix(),
                request.getMethod(),
                request.getRequestURI(),
                resolveActorKey(request),
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
            List<String> fieldNames = new ArrayList<>();
            fieldNames.addAll(node.propertyNames());
            fieldNames.stream()
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
                || argument instanceof MultipartFile[]
                || argument instanceof BindingResult
                || argument instanceof Principal
                || argument instanceof InputStreamSource
                || argument instanceof Reader
                || argument instanceof Writer
                || argument instanceof InputStream
                || argument instanceof OutputStream
                || argument instanceof Locale);
    }

    private String resolveActorKey(HttpServletRequest request) {
        if (StpUtil.isLogin()) {
            return "sys-" + Objects.toString(StpUtil.getLoginId(), "anonymous");
        }
        if (StpMemberUtil.isLogin()) {
            return "member-" + Objects.toString(StpMemberUtil.getLoginId(), "anonymous");
        }
        return "ip-" + resolveClientIp(request);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

}
