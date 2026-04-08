package com.zhanglx.sso.log.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 操作日志展示对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "OperationLogVO", description = "操作日志")
public class OperationLogVO {

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
