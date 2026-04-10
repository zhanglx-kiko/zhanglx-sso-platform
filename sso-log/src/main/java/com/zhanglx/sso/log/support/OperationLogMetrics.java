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
    /**
     * 指标注册器提供者。
     */
    private final ObjectProvider<MeterRegistry> meterRegistryProvider;
    /**
     * acceptedCount。
     */
    private final AtomicLong acceptedCount = new AtomicLong();
    /**
     * successWriteCount。
     */
    private final AtomicLong successWriteCount = new AtomicLong();
    /**
     * failedWriteCount。
     */
    private final AtomicLong failedWriteCount = new AtomicLong();
    /**
     * 队列满时丢弃计数。
     */
    private final AtomicLong queueFullDropCount = new AtomicLong();
    /**
     * lastFailureTime。
     */
    private final AtomicReference<LocalDateTime> lastFailureTime = new AtomicReference<>();
    /**
     * 最后失败原因。
     */
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

    /**
     * incrementCounter处理逻辑。
     */
    private void incrementCounter(String name, double amount) {
        MeterRegistry registry = meterRegistryProvider.getIfAvailable();
        if (registry == null) {
            return;
        }
        Counter counter = registry.counter(name);
        counter.increment(amount);
    }
}