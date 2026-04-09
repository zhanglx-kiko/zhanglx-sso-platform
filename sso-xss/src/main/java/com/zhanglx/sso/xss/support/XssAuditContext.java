package com.zhanglx.sso.xss.support;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 单次请求的 XSS 审计上下文。
 * 这里先在请求内聚合，再在请求结束时统一落到 Micrometer，避免同一请求里频繁打指标。
 */
final class XssAuditContext {

    private final Map<AuditKey, Integer> hitBuckets = new HashMap<>();

    private final EnumMap<XssBypassReason, Integer> bypassBuckets = new EnumMap<>(XssBypassReason.class);

    private final Set<String> onceKeys = new HashSet<>();

    private boolean flushed;

    void recordHit(XssInputSource inputSource, XssPolicyMode policyMode) {
        AuditKey auditKey = new AuditKey(inputSource, policyMode);
        hitBuckets.merge(auditKey, 1, Integer::sum);
    }

    void recordBypass(XssBypassReason bypassReason) {
        bypassBuckets.merge(bypassReason, 1, Integer::sum);
    }

    void recordBypassOnce(XssBypassReason bypassReason, String fingerprint) {
        String onceKey = bypassReason.metricValue() + ":" + fingerprint;
        if (onceKeys.add(onceKey)) {
            recordBypass(bypassReason);
        }
    }

    boolean hasAnyHit() {
        return !hitBuckets.isEmpty() || !bypassBuckets.isEmpty();
    }

    Map<AuditKey, Integer> hitBuckets() {
        return hitBuckets;
    }

    Map<XssBypassReason, Integer> bypassBuckets() {
        return bypassBuckets;
    }

    boolean flushed() {
        return flushed;
    }

    void markFlushed() {
        this.flushed = true;
    }

    record AuditKey(XssInputSource inputSource, XssPolicyMode policyMode) {
    }
}
