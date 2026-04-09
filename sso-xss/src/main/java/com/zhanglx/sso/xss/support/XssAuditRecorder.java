package com.zhanglx.sso.xss.support;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

/**
 * 请求级 XSS 审计记录器。
 */
@Component
@RequiredArgsConstructor
public class XssAuditRecorder {

    private static final String AUDIT_CONTEXT_ATTRIBUTE = XssAuditRecorder.class.getName() + ".AUDIT_CONTEXT";

    private final XssAuditMetrics xssAuditMetrics;

    public void recordHit(HttpServletRequest request, XssInputSource inputSource, XssPolicyMode policyMode) {
        if (request == null) {
            return;
        }
        getContext(request).recordHit(inputSource, policyMode);
    }

    public void recordBypass(HttpServletRequest request, XssBypassReason bypassReason) {
        if (request == null) {
            return;
        }
        getContext(request).recordBypass(bypassReason);
    }

    public void recordBypassOnce(HttpServletRequest request, XssBypassReason bypassReason, String fingerprint) {
        if (request == null || !StringUtils.hasText(fingerprint)) {
            return;
        }
        getContext(request).recordBypassOnce(bypassReason, fingerprint);
    }

    public void flush(HttpServletRequest request) {
        if (request == null) {
            return;
        }
        Object attribute = request.getAttribute(AUDIT_CONTEXT_ATTRIBUTE);
        if (!(attribute instanceof XssAuditContext context) || context.flushed() || !context.hasAnyHit()) {
            return;
        }

        String endpoint = resolveEndpoint(request);
        for (Map.Entry<XssAuditContext.AuditKey, Integer> entry : context.hitBuckets().entrySet()) {
            xssAuditMetrics.incrementHit(
                    endpoint,
                    entry.getKey().inputSource(),
                    entry.getKey().policyMode(),
                    entry.getValue()
            );
        }
        for (Map.Entry<XssBypassReason, Integer> entry : context.bypassBuckets().entrySet()) {
            xssAuditMetrics.incrementWhitelist(endpoint, entry.getKey(), entry.getValue());
        }
        xssAuditMetrics.incrementRequestHit(endpoint);
        context.markFlushed();
    }

    private XssAuditContext getContext(HttpServletRequest request) {
        Object attribute = request.getAttribute(AUDIT_CONTEXT_ATTRIBUTE);
        if (attribute instanceof XssAuditContext context) {
            return context;
        }
        XssAuditContext context = new XssAuditContext();
        request.setAttribute(AUDIT_CONTEXT_ATTRIBUTE, context);
        return context;
    }

    private String resolveEndpoint(HttpServletRequest request) {
        Object attribute = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (attribute instanceof String pattern && StringUtils.hasText(pattern)) {
            return pattern;
        }
        return "UNKNOWN";
    }
}
