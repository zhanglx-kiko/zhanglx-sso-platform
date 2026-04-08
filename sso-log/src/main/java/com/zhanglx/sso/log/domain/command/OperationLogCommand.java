package com.zhanglx.sso.log.domain.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 操作日志采集命令。
 * AOP、手动 API、外部上报都统一收敛到这个模型，便于后续扩展和限流。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogCommand {

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
    private Map<String, Object> ext;
}
