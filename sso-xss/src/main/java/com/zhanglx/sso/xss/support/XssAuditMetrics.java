package com.zhanglx.sso.xss.support;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * XSS 审计指标。
 * 这里同时保留内存计数和 Micrometer Counter，方便后续对接 Prometheus 与排查当前实例状态。
 */
@Getter
@Component
public class XssAuditMetrics {

    private static final String UNKNOWN_ENDPOINT = "UNKNOWN";

    private final ObjectProvider<MeterRegistry> meterRegistryProvider;

    private final AtomicLong hitCount = new AtomicLong();

    private final AtomicLong whitelistHitCount = new AtomicLong();

    private final AtomicLong requestHitCount = new AtomicLong();

    private final Map<String, AtomicLong> endpointHitDistribution = new ConcurrentHashMap<>();

    public XssAuditMetrics(ObjectProvider<MeterRegistry> meterRegistryProvider) {
        this.meterRegistryProvider = meterRegistryProvider;
    }

    public void incrementHit(String endpoint, XssInputSource inputSource, XssPolicyMode policyMode, long count) {
        if (count <= 0) {
            return;
        }
        String normalizedEndpoint = normalizeEndpoint(endpoint);
        hitCount.addAndGet(count);
        incrementCounter(
                "sso.xss.hit.total",
                count,
                "endpoint", normalizedEndpoint,
                "source", inputSource.metricValue(),
                "policy", policyMode.metricValue()
        );
    }

    public void incrementWhitelist(String endpoint, XssBypassReason bypassReason, long count) {
        if (count <= 0) {
            return;
        }
        String normalizedEndpoint = normalizeEndpoint(endpoint);
        whitelistHitCount.addAndGet(count);
        incrementCounter(
                "sso.xss.hit.whitelist",
                count,
                "endpoint", normalizedEndpoint,
                "reason", bypassReason.metricValue()
        );
    }

    public void incrementRequestHit(String endpoint) {
        String normalizedEndpoint = normalizeEndpoint(endpoint);
        requestHitCount.incrementAndGet();
        endpointHitDistribution.computeIfAbsent(normalizedEndpoint, key -> new AtomicLong()).incrementAndGet();
        incrementCounter(
                "sso.xss.request.hit",
                1,
                "endpoint", normalizedEndpoint
        );
    }

    private void incrementCounter(String name, double amount, String... tags) {
        MeterRegistry meterRegistry = meterRegistryProvider.getIfAvailable();
        if (meterRegistry == null) {
            return;
        }
        Counter counter = meterRegistry.counter(name, tags);
        counter.increment(amount);
    }

    private String normalizeEndpoint(String endpoint) {
        return StringUtils.hasText(endpoint) ? endpoint : UNKNOWN_ENDPOINT;
    }
}
