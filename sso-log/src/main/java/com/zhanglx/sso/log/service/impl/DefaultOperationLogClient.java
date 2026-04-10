package com.zhanglx.sso.log.service.impl;

import com.zhanglx.sso.log.config.OperationLogProperties;
import com.zhanglx.sso.log.domain.command.OperationLogCommand;
import com.zhanglx.sso.log.domain.model.OperationLogDocument;
import com.zhanglx.sso.log.infrastructure.queue.OperationLogDispatcher;
import com.zhanglx.sso.log.service.OperationLogClient;
import com.zhanglx.sso.log.support.OperationLogContextResolver;
import com.zhanglx.sso.log.support.OperationLogSummarySanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 默认操作日志客户端实现。
 */
@Service
@RequiredArgsConstructor
public class DefaultOperationLogClient implements OperationLogClient {
    /**
     * 配置属性。
     */
    private final OperationLogProperties properties;
    /**
     * contextResolver。
     */
    private final OperationLogContextResolver contextResolver;
    /**
     * 摘要脱敏器。
     */
    private final OperationLogSummarySanitizer summarySanitizer;
    /**
     * dispatcher。
     */
    private final OperationLogDispatcher dispatcher;

    @Override
    public void record(OperationLogCommand command) {
        if (!properties.isEnableOperationLog() || command == null) {
            return;
        }

        OperationLogCommand enriched = contextResolver.enrich(command);
        dispatcher.offer(OperationLogDocument.builder()
                .logId(UUID.randomUUID().toString().replace("-", ""))
                .appCode(enriched.getAppCode())
                .appName(enriched.getAppName())
                .platformCode(enriched.getPlatformCode())
                .platformName(enriched.getPlatformName())
                .module(enriched.getModule())
                .feature(enriched.getFeature())
                .operationType(enriched.getOperationType())
                .operationName(enriched.getOperationName())
                .operationDesc(enriched.getOperationDesc())
                .userId(enriched.getUserId())
                .username(enriched.getUsername())
                .displayName(enriched.getDisplayName())
                .tenantId(enriched.getTenantId())
                .requestMethod(enriched.getRequestMethod())
                .requestPath(enriched.getRequestPath())
                .requestQuery(enriched.getRequestQuery())
                .requestBodySummary(enriched.getRequestBodySummary())
                .responseSummary(enriched.getResponseSummary())
                .resultStatus(enriched.getResultStatus())
                .errorCode(enriched.getErrorCode())
                .errorMessageSummary(enriched.getErrorMessageSummary())
                .exceptionType(enriched.getExceptionType())
                .exceptionStackSummary(enriched.getExceptionStackSummary())
                .clientIp(enriched.getClientIp())
                .userAgent(summarySanitizer.truncateUserAgent(enriched.getUserAgent()))
                .traceId(enriched.getTraceId())
                .requestId(enriched.getRequestId())
                .startTime(enriched.getStartTime())
                .endTime(enriched.getEndTime())
                .durationMs(enriched.getDurationMs())
                .sourceSystem(enriched.getSourceSystem())
                .ext(summarySanitizer.sanitizeExt(enriched.getExt()))
                .ingestTime(LocalDateTime.now())
                .build());
    }
}