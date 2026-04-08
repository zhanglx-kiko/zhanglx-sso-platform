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

@Slf4j
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 40)
@RequiredArgsConstructor
public class RequestRateLimitAspect {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final RateLimitProperties properties;
    private final RequestProtectionStore requestProtectionStore;
    private final WebExpressionEvaluator webExpressionEvaluator;
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
        if (!decision.isAllowed()) {
            log.warn("request rate limited, uri={}, method={}, clientIp={}, key={}, current={}, limit={}, reset={}s",
                    request.getRequestURI(),
                    request.getMethod(),
                    clientIp,
                    key,
                    decision.getCurrent(),
                    decision.getLimit(),
                    decision.getResetSeconds());
            throw rateLimitedException(requestRateLimit);
        }
        return joinPoint.proceed();
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    private HttpServletResponse currentResponse() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getResponse();
    }

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

    private void writeHeaders(RequestRateLimit requestRateLimit, RateLimitDecision decision) {
        if (!properties.isWriteResponseHeaders() || !requestRateLimit.writeHeaders()) {
            return;
        }
        HttpServletResponse response = currentResponse();
        if (response == null) {
            return;
        }
        response.setHeader("X-RateLimit-Limit", String.valueOf(decision.getLimit()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(decision.getRemaining()));
        response.setHeader("X-RateLimit-Reset-Seconds", String.valueOf(decision.getResetSeconds()));
        if (!decision.isAllowed()) {
            response.setHeader("Retry-After", String.valueOf(decision.getResetSeconds()));
        }
    }

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

    private String defaultValue(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
