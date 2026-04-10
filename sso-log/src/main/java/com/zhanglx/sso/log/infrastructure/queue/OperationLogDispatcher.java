package com.zhanglx.sso.log.infrastructure.queue;

import com.zhanglx.sso.log.config.OperationLogProperties;
import com.zhanglx.sso.log.domain.model.OperationLogDocument;
import com.zhanglx.sso.log.infrastructure.es.OperationLogElasticsearchClient;
import com.zhanglx.sso.log.support.OperationLogMetrics;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 操作日志异步分发器。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperationLogDispatcher {
    /**
     * 配置属性。
     */
    private final OperationLogProperties properties;
    /**
     * Elasticsearch 客户端。
     */
    private final OperationLogElasticsearchClient elasticsearchClient;
    /**
     * metrics。
     */
    private final OperationLogMetrics metrics;
    /**
     * running。
     */
    private final AtomicBoolean running = new AtomicBoolean(false);
    /**
     * workerCount。
     */
    private final AtomicInteger workerCount = new AtomicInteger();
    /**
     * 事件队列。
     */
    private BlockingQueue<OperationLogDocument> eventQueue;
    /**
     * workerExecutor。
     */
    private ThreadPoolExecutor workerExecutor;
    /**
     * lastQueueFullLogAt。
     */
    private volatile long lastQueueFullLogAt;
    /**
     * 最后失败记录时间。
     */
    private volatile long lastFailureLogAt;

    @PostConstruct
    public void start() {
        if (!properties.isEnableOperationLog()) {
            log.info("操作日志功能已关闭，跳过后台消费者初始化");
            return;
        }

        OperationLogProperties.AsyncProperties asyncProperties = properties.getAsync();
        int corePoolSize = Math.max(1, asyncProperties.getCorePoolSize());
        int maxPoolSize = Math.max(corePoolSize, asyncProperties.getMaxPoolSize());
        eventQueue = new ArrayBlockingQueue<>(Math.max(128, asyncProperties.getQueueCapacity()));
        workerExecutor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                asyncProperties.getKeepAliveSeconds(),
                TimeUnit.SECONDS,
                // 消费线程是长生命周期任务，这里必须直接移交，避免“扩容任务”被排队后迟迟不生效。
                new SynchronousQueue<>(),
                new ThreadFactory() {
                    /**
                     * 线程计数器。
                     */
                    private final AtomicInteger counter = new AtomicInteger(1);

                    @Override
                    public Thread newThread(Runnable runnable) {
                        Thread thread = new Thread(runnable);
                        thread.setName("operation-log-worker-" + counter.getAndIncrement());
                        thread.setDaemon(true);
                        return thread;
                    }
                },
                new ThreadPoolExecutor.AbortPolicy()
        );
        running.set(true);
        for (int i = 0; i < corePoolSize; i++) {
            submitWorker(false);
        }
        log.info("操作日志后台分发器已启动，corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                corePoolSize,
                maxPoolSize,
                asyncProperties.getQueueCapacity());
    }

    @PreDestroy
    public void stop() {
        running.set(false);
        if (workerExecutor != null) {
            workerExecutor.shutdownNow();
            try {
                if (!workerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("操作日志后台线程池未在预期时间内停止");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void offer(OperationLogDocument document) {
        if (!properties.isEnableOperationLog() || document == null) {
            return;
        }

        if (eventQueue.offer(document)) {
            metrics.incrementAccepted();
            tryScaleOut();
            return;
        }

        String rejectionPolicy = properties.getAsync().getRejectionPolicy();
        if ("DROP_OLDEST".equalsIgnoreCase(rejectionPolicy)) {
            eventQueue.poll();
            if (eventQueue.offer(document)) {
                metrics.incrementAccepted();
                metrics.incrementQueueDrop("操作日志队列已满，已淘汰最旧日志");
                logQueueFullOnce("操作日志队列已满，按 DROP_OLDEST 策略淘汰最旧日志");
                tryScaleOut();
                return;
            }
        }

        metrics.incrementQueueDrop("操作日志队列已满，当前日志被丢弃");
        logQueueFullOnce("操作日志队列已满，当前日志被丢弃，未阻塞主业务线程");
    }

    /**
     * 尝试扩容处理逻辑。
     */
    private void tryScaleOut() {
        int maxPoolSize = Math.max(properties.getAsync().getCorePoolSize(), properties.getAsync().getMaxPoolSize());
        int threshold = Math.max(1, properties.getElasticsearch().getBulkBatchSize() * 2);
        if (eventQueue.size() < threshold || workerCount.get() >= maxPoolSize) {
            return;
        }
        submitWorker(true);
    }

    /**
     * 提交流程处理器处理逻辑。
     */
    private void submitWorker(boolean burstWorker) {
        workerCount.incrementAndGet();
        try {
            workerExecutor.execute(() -> consumeLoop(burstWorker));
        } catch (RejectedExecutionException e) {
            workerCount.decrementAndGet();
            logFailureOnce("启动操作日志消费者失败：" + e.getMessage(), e);
        }
    }

    /**
     * consumeLoop处理逻辑。
     */
    private void consumeLoop(boolean burstWorker) {
        long keepAliveMillis = Duration.ofSeconds(Math.max(1, properties.getAsync().getKeepAliveSeconds())).toMillis();
        long flushIntervalMillis = Math.max(200L, properties.getElasticsearch().getBulkFlushIntervalMs());
        long lastActiveAt = System.currentTimeMillis();
        List<OperationLogDocument> batch = new ArrayList<>(properties.getElasticsearch().getBulkBatchSize());
        int estimatedBytes = 0;

        try {
            while (running.get() || !eventQueue.isEmpty()) {
                OperationLogDocument document = eventQueue.poll(flushIntervalMillis, TimeUnit.MILLISECONDS);
                if (document == null) {
                    flush(batch);
                    batch.clear();
                    estimatedBytes = 0;
                    if (burstWorker
                            && workerCount.get() > properties.getAsync().getCorePoolSize()
                            && eventQueue.isEmpty()
                            && System.currentTimeMillis() - lastActiveAt >= keepAliveMillis) {
                        return;
                    }
                    continue;
                }

                lastActiveAt = System.currentTimeMillis();
                batch.add(document);
                estimatedBytes += estimateDocumentBytes(document);
                if (batch.size() >= properties.getElasticsearch().getBulkBatchSize()
                        || estimatedBytes >= properties.getElasticsearch().getBulkMaxBytes()) {
                    flush(batch);
                    batch.clear();
                    estimatedBytes = 0;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (!batch.isEmpty()) {
                flush(batch);
            }
            workerCount.decrementAndGet();
        }
    }

    /**
     * flush处理逻辑。
     */
    private void flush(List<OperationLogDocument> batch) {
        if (batch == null || batch.isEmpty()) {
            return;
        }

        List<OperationLogDocument> currentBatch = new ArrayList<>(batch);
        int retryCount = 0;
        while (!currentBatch.isEmpty()) {
            try {
                OperationLogElasticsearchClient.BulkWriteResult result = elasticsearchClient.bulkWrite(currentBatch);
                int successCount = currentBatch.size() - result.failedDocuments().size();
                metrics.incrementSuccess(successCount);
                if (result.failedDocuments().isEmpty()) {
                    return;
                }

                retryCount++;
                if (retryCount > properties.getElasticsearch().getMaxRetryCount()) {
                    metrics.incrementFailure(result.failedDocuments().size(), result.failureSummary());
                    logFailureOnce("操作日志 bulk 写入存在部分失败，达到最大重试次数后已丢弃：" + result.failureSummary(), null);
                    return;
                }

                currentBatch = result.failedDocuments();
                sleepBackoff(retryCount);
            } catch (Exception e) {
                retryCount++;
                if (retryCount > properties.getElasticsearch().getMaxRetryCount()) {
                    metrics.incrementFailure(currentBatch.size(), e.getMessage());
                    logFailureOnce("操作日志 bulk 写入失败，达到最大重试次数后已丢弃：" + e.getMessage(), e);
                    return;
                }
                sleepBackoff(retryCount);
            }
        }
    }

    /**
     * 退避休眠处理逻辑。
     */
    private void sleepBackoff(int retryCount) {
        try {
            Thread.sleep(Math.min(1000L, 200L * retryCount));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * estimateDocumentBytes处理逻辑。
     */
    private int estimateDocumentBytes(OperationLogDocument document) {
        int base = 256;
        base += safeLength(document.getOperationDesc());
        base += safeLength(document.getRequestBodySummary());
        base += safeLength(document.getResponseSummary());
        base += safeLength(document.getErrorMessageSummary());
        base += safeLength(document.getExceptionStackSummary());
        return base;
    }

    /**
     * safeLength处理逻辑。
     */
    private int safeLength(String value) {
        return value == null ? 0 : value.length();
    }

    /**
     * 记录一次队列满日志处理逻辑。
     */
    private void logQueueFullOnce(String message) {
        long now = System.currentTimeMillis();
        if (now - lastQueueFullLogAt < 30_000L) {
            return;
        }
        lastQueueFullLogAt = now;
        log.warn(message);
    }

    /**
     * 记录一次失败日志处理逻辑。
     */
    private void logFailureOnce(String message, Exception exception) {
        long now = System.currentTimeMillis();
        if (now - lastFailureLogAt < 30_000L) {
            return;
        }
        lastFailureLogAt = now;
        if (exception == null) {
            log.error(message);
        } else {
            log.error(message, exception);
        }
    }
}