package com.zhanglx.sso.log.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 最终写入 ES 的文档模型。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogDocument {

    private String logId;
    private String appCode;
    private String appName;
    private String platformCode;
    private String platformName;
    private String module;
    private String feature;
    private String operationType;
    private String operationName;
    private String operationDesc;
    private String userId;
    private String username;
    private String displayName;
    private String tenantId;
    private String requestMethod;
    private String requestPath;
    private String requestQuery;
    private String requestBodySummary;
    private String responseSummary;
    private String resultStatus;
    private String errorCode;
    private String errorMessageSummary;
    private String exceptionType;
    private String exceptionStackSummary;
    private String clientIp;
    private String userAgent;
    private String traceId;
    private String requestId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMs;
    private String sourceSystem;
    private Map<String, String> ext;
    private LocalDateTime ingestTime;
}
