package com.zhanglx.sso.log.support;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 操作日志链路统计。
 */
@Getter
@Component
public class OperationLogMetrics {

    private final ObjectProvider<MeterRegistry> meterRegistryProvider;

    private final AtomicLong acceptedCount = new AtomicLong();
    private final AtomicLong successWriteCount = new AtomicLong();
    private final AtomicLong failedWriteCount = new AtomicLong();
    private final AtomicLong queueFullDropCount = new AtomicLong();
    private final AtomicReference<LocalDateTime> lastFailureTime = new AtomicReference<>();
    private final AtomicReference<String> lastFailureReason = new AtomicReference<>();

    public OperationLogMetrics(ObjectProvider<MeterRegistry> meterRegistryProvider) {
        this.meterRegistryProvider = meterRegistryProvider;
    }

    public void incrementAccepted() {
        acceptedCount.incrementAndGet();
        incrementCounter("sso.operation.log.accepted", 1);
    }

    public void incrementSuccess(long count) {
        if (count <= 0) {
            return;
        }
        successWriteCount.addAndGet(count);
        incrementCounter("sso.operation.log.write.success", count);
    }

    public void incrementFailure(long count, String reason) {
        if (count <= 0) {
            return;
        }
        failedWriteCount.addAndGet(count);
        lastFailureTime.set(LocalDateTime.now());
        lastFailureReason.set(reason);
        incrementCounter("sso.operation.log.write.failure", count);
    }

    public void incrementQueueDrop(String reason) {
        queueFullDropCount.incrementAndGet();
        lastFailureTime.set(LocalDateTime.now());
        lastFailureReason.set(reason);
        incrementCounter("sso.operation.log.queue.drop", 1);
    }

    private void incrementCounter(String name, double amount) {
        MeterRegistry registry = meterRegistryProvider.getIfAvailable();
        if (registry == null) {
            return;
        }
        Counter counter = registry.counter(name);
        counter.increment(amount);
    }
}
