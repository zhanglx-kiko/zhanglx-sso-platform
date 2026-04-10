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
    /**
     * 日志标识。
     */
    private String logId;
    /**
     * 应用编码。
     */
    private String appCode;
    /**
     * 应用名称。
     */
    private String appName;
    /**
     * 平台编码。
     */
    private String platformCode;
    /**
     * 平台名称。
     */
    private String platformName;
    /**
     * 模块名称。
     */
    private String module;
    /**
     * 功能名称。
     */
    private String feature;
    /**
     * 操作类型。
     */
    private String operationType;
    /**
     * 操作名称。
     */
    private String operationName;
    /**
     * 操作描述。
     */
    private String operationDesc;
    /**
     * 用户标识。
     */
    private String userId;
    /**
     * 用户名。
     */
    private String username;
    /**
     * 显示名称。
     */
    private String displayName;
    /**
     * 租户标识。
     */
    private String tenantId;
    /**
     * 请求方法。
     */
    private String requestMethod;
    /**
     * 请求路径。
     */
    private String requestPath;
    /**
     * 请求查询串。
     */
    private String requestQuery;
    /**
     * 请求体摘要。
     */
    private String requestBodySummary;
    /**
     * 响应摘要。
     */
    private String responseSummary;
    /**
     * 结果状态。
     */
    private String resultStatus;
    /**
     * 错误码。
     */
    private String errorCode;
    /**
     * 错误信息摘要。
     */
    private String errorMessageSummary;
    /**
     * 异常类型。
     */
    private String exceptionType;
    /**
     * 异常堆栈摘要。
     */
    private String exceptionStackSummary;
    /**
     * 客户端地址。
     */
    private String clientIp;
    /**
     * 用户代理。
     */
    private String userAgent;
    /**
     * 链路追踪标识。
     */
    private String traceId;
    /**
     * 请求标识。
     */
    private String requestId;
    /**
     * 开始时间。
     */
    private LocalDateTime startTime;
    /**
     * 结束时间。
     */
    private LocalDateTime endTime;
    /**
     * 耗时毫秒数。
     */
    private Long durationMs;
    /**
     * 来源系统。
     */
    private String sourceSystem;
    /**
     * 扩展信息。
     */
    private Map<String, String> ext;
    /**
     * 入库时间。
     */
    private LocalDateTime ingestTime;
}