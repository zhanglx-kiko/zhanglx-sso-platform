package com.zhanglx.sso.web.support;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class RequestIdentityAccessor {

    private final ObjectProvider<RequestActorResolver> requestActorResolverProvider;
    private final ServletClientIpResolver servletClientIpResolver;

    public String resolveActorKey(HttpServletRequest request) {
        String resolved = resolveUsingResolver(request, ResolverType.ACTOR);
        if (StringUtils.hasText(resolved)) {
            return resolved;
        }
        String userId = resolveUserId(request);
        if (StringUtils.hasText(userId)) {
            return "user:" + userId;
        }
        if (request != null && request.getUserPrincipal() != null && StringUtils.hasText(request.getUserPrincipal().getName())) {
            return "principal:" + request.getUserPrincipal().getName().trim();
        }
        return "ip:" + defaultValue(resolveClientIp(request), "unknown");
    }

    public String resolveUserId(HttpServletRequest request) {
        String resolved = resolveUsingResolver(request, ResolverType.USER_ID);
        if (StringUtils.hasText(resolved)) {
            return resolved;
        }
        if (request != null && request.getUserPrincipal() != null && StringUtils.hasText(request.getUserPrincipal().getName())) {
            return request.getUserPrincipal().getName().trim();
        }
        return null;
    }

    public String resolveToken(HttpServletRequest request) {
        String resolved = resolveUsingResolver(request, ResolverType.TOKEN);
        if (StringUtils.hasText(resolved)) {
            return resolved;
        }
        if (request == null) {
            return null;
        }
        String token = request.getHeader("token");
        if (StringUtils.hasText(token)) {
            return token.trim();
        }
        String authorization = request.getHeader("Authorization");
        if (StringUtils.hasText(authorization)) {
            String normalized = authorization.trim();
            if (normalized.regionMatches(true, 0, "Bearer ", 0, 7)) {
                normalized = normalized.substring(7).trim();
            }
            if (StringUtils.hasText(normalized)) {
                return normalized;
            }
        }
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (cookie != null && "token".equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                return cookie.getValue().trim();
            }
        }
        return null;
    }

    public String resolveTenantId(HttpServletRequest request) {
        String resolved = resolveUsingResolver(request, ResolverType.TENANT_ID);
        if (StringUtils.hasText(resolved)) {
            return resolved;
        }
        return servletClientIpResolver.resolveTenantId(request);
    }

    public String resolveClientIp(HttpServletRequest request) {
        return servletClientIpResolver.resolveClientIp(request);
    }

    private String resolveUsingResolver(HttpServletRequest request, ResolverType resolverType) {
        return requestActorResolverProvider.orderedStream()
                .map(resolver -> switch (resolverType) {
                    case ACTOR -> resolver.resolveActor(request);
                    case USER_ID -> resolver.resolveUserId(request);
                    case TOKEN -> resolver.resolveToken(request);
                    case TENANT_ID -> resolver.resolveTenantId(request);
                })
                .filter(StringUtils::hasText)
                .map(String::trim)
                .findFirst()
                .orElse(null);
    }

    private String defaultValue(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private enum ResolverType {
        ACTOR,
        USER_ID,
        TOKEN,
        TENANT_ID
    }
}
