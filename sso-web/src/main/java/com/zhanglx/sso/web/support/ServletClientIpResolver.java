package com.zhanglx.sso.web.support;

import com.zhanglx.sso.common.net.ClientIpUtils;
import com.zhanglx.sso.web.config.ClientIpProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * ServletClient地址解析器。
 */
@Component
@RequiredArgsConstructor
public class ServletClientIpResolver {
    /**
     * 配置属性。
     */
    private final ClientIpProperties properties;

    public String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        return ClientIpUtils.resolveClientIp(
                request.getRemoteAddr(),
                request.getHeader(properties.getForwardedForHeader()),
                request.getHeader(properties.getRealIpHeader()),
                properties.getTrustedProxies()
        );
    }

    public String resolveTenantId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String tenantId = request.getHeader(properties.getTenantHeader());
        return StringUtils.hasText(tenantId) ? tenantId.trim() : null;
    }
}