package com.zhanglx.sso.web.aspect;

import com.zhanglx.sso.common.ResultCode;
import com.zhanglx.sso.common.net.ClientIpUtils;
import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.web.annotation.RateLimitDimension;
import com.zhanglx.sso.web.annotation.RequestRateLimit;
import com.zhanglx.sso.web.config.RateLimitProperties;
import com.zhanglx.sso.web.exception.WebRequestProtectionErrorCode;
import com.zhanglx.sso.web.support.RateLimitDecision;
import com.zhanglx.sso.web.support.RequestIdentityAccessor;
import com.zhanglx.sso.web.support.RequestProtectionStore;
import com.zhanglx.sso.web.support.WebExpressionEvaluator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 请求限流切面。
 */
@Slf4j
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 40)
@RequiredArgsConstructor
public class RequestRateLimitAspect {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    /**
     * 配置属性。
     */
    private final RateLimitProperties properties;
    /**
     * 请求防护存储器。
     */
    private final RequestProtectionStore requestProtectionStore;
    /**
     * Web 表达式解析器。
     */
    private final WebExpressionEvaluator webExpressionEvaluator;
    /**
     * 请求标识访问器。
     */
    private final RequestIdentityAccessor requestIdentityAccessor;

    @Around("@annotation(requestRateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RequestRateLimit requestRateLimit) throws Throwable {
        if (!properties.isEnabled()) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = currentRequest();
        if (request == null) {
            return joinPoint.proceed();
        }
        if (!webExpressionEvaluator.matchesCondition(requestRateLimit.condition(), joinPoint, request)) {
            return joinPoint.proceed();
        }

        String clientIp = requestIdentityAccessor.resolveClientIp(request);
        if (isWhitelisted(request.getRequestURI(), clientIp)) {
            return joinPoint.proceed();
        }

        long limit = requestRateLimit.limit() > 0L ? requestRateLimit.limit() : properties.getDefaultLimit();
        Duration window = Duration.ofSeconds(Math.max(1L,
                requestRateLimit.windowSeconds() > 0L
                        ? requestRateLimit.windowSeconds()
                        : properties.getDefaultWindowSeconds()));
        String key = buildKey(joinPoint, request, requestRateLimit);
        RateLimitDecision decision = requestProtectionStore.acquireRateLimit(
                key,
                limit,
                window,
                properties.isLocalFallbackEnabled()
        );
        writeHeaders(requestRateLimit, decision);
        if (!decision.allowed()) {
            log.warn("request rate limited, uri={}, method={}, clientIp={}, key={}, current={}, limit={}, reset={}s",
                    request.getRequestURI(),
                    request.getMethod(),
                    clientIp,
                    key,
                    decision.current(),
                    decision.limit(),
                    decision.resetSeconds());
            throw rateLimitedException(requestRateLimit);
        }
        return joinPoint.proceed();
    }

    /**
     * 获取当前请求对象。
     */
    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    /**
     * 获取当前响应对象。
     */
    private HttpServletResponse currentResponse() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getResponse();
    }

    /**
     * 构建key。
     */
    private String buildKey(ProceedingJoinPoint joinPoint, HttpServletRequest request, RequestRateLimit requestRateLimit) {
        List<String> keyParts = new ArrayList<>();
        for (RateLimitDimension dimension : requestRateLimit.dimensions()) {
            keyParts.add(dimension.name().toLowerCase(Locale.ROOT) + "=" + resolveDimensionValue(dimension, request));
        }
        String customKey = webExpressionEvaluator.evaluateAsString(requestRateLimit.customKey(), joinPoint, request);
        if (StringUtils.hasText(customKey)) {
            keyParts.add("custom=" + customKey);
        }
        if (keyParts.isEmpty()) {
            keyParts.add("uri=" + request.getRequestURI());
        }
        String rawKey = String.join("|", keyParts);
        String fingerprint = DigestUtils.md5DigestAsHex(rawKey.getBytes(StandardCharsets.UTF_8));
        return properties.getKeyPrefix() + ":" + fingerprint;
    }

    /**
     * 解析dimensionValue。
     */
    private String resolveDimensionValue(RateLimitDimension dimension, HttpServletRequest request) {
        return switch (dimension) {
            case IP -> defaultValue(requestIdentityAccessor.resolveClientIp(request), "unknown");
            case USER_ID -> defaultValue(requestIdentityAccessor.resolveUserId(request), "anonymous");
            case TOKEN -> defaultValue(requestIdentityAccessor.resolveToken(request), "anonymous");
            case URI -> defaultValue(request.getRequestURI(), "unknown");
            case METHOD -> defaultValue(request.getMethod(), "UNKNOWN");
            case TENANT_ID -> defaultValue(requestIdentityAccessor.resolveTenantId(request), "default");
        };
    }

    /**
     * 是否whitelisted处理逻辑。
     */
    private boolean isWhitelisted(String requestUri, String clientIp) {
        boolean pathWhitelisted = properties.getWhitelistPaths().stream()
                .filter(StringUtils::hasText)
                .anyMatch(pattern -> PATH_MATCHER.match(pattern.trim(), requestUri));
        if (pathWhitelisted) {
            return true;
        }
        return properties.getWhitelistIps().stream()
                .filter(StringUtils::hasText)
                .anyMatch(pattern -> ClientIpUtils.isTrustedProxy(clientIp, List.of(pattern.trim())));
    }

    /**
     * 写出headers。
     */
    private void writeHeaders(RequestRateLimit requestRateLimit, RateLimitDecision decision) {
        if (!properties.isWriteResponseHeaders() || !requestRateLimit.writeHeaders()) {
            return;
        }
        HttpServletResponse response = currentResponse();
        if (response == null) {
            return;
        }
        response.setHeader("X-RateLimit-Limit", String.valueOf(decision.limit()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(decision.remaining()));
        response.setHeader("X-RateLimit-Reset-Seconds", String.valueOf(decision.resetSeconds()));
        if (!decision.allowed()) {
            response.setHeader("Retry-After", String.valueOf(decision.resetSeconds()));
        }
    }

    /**
     * rateLimitedException处理逻辑。
     */
    private BusinessException rateLimitedException(RequestRateLimit requestRateLimit) {
        if (StringUtils.hasText(requestRateLimit.messageKey())) {
            return new BusinessException(ResultCode.TOO_MANY_REQUESTS.getCode(), requestRateLimit.messageKey().trim());
        }
        if (StringUtils.hasText(requestRateLimit.message())) {
            return new BusinessException(ResultCode.TOO_MANY_REQUESTS.getCode(), requestRateLimit.message().trim());
        }
        if (StringUtils.hasText(properties.getDefaultMessageKey())) {
            return new BusinessException(ResultCode.TOO_MANY_REQUESTS.getCode(), properties.getDefaultMessageKey().trim());
        }
        return new BusinessException(WebRequestProtectionErrorCode.RATE_LIMITED);
    }

    /**
     * 返回默认值。
     */
    private String defaultValue(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}