package com.zhanglx.sso.log.support;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.zhanglx.sso.common.trace.TraceConstants;
import com.zhanglx.sso.core.trace.TraceContextHolder;
import com.zhanglx.sso.core.utils.satoken.StpMemberUtil;
import com.zhanglx.sso.log.annotation.OperationLog;
import com.zhanglx.sso.log.config.OperationLogProperties;
import com.zhanglx.sso.log.constants.LogSessionKeys;
import com.zhanglx.sso.log.domain.command.OperationLogCommand;
import com.zhanglx.sso.web.support.RequestIdentityAccessor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 统一补齐操作日志上下文。
 */
@Component
@RequiredArgsConstructor
public class OperationLogContextResolver {

    public static final String APP_CODE_HEADER = "X-App-Code";
    public static final String APP_NAME_HEADER = "X-App-Name";
    public static final String PLATFORM_CODE_HEADER = "X-Platform-Code";
    public static final String PLATFORM_NAME_HEADER = "X-Platform-Name";
    public static final String SOURCE_SYSTEM_HEADER = "X-Source-System";
    /**
     * 配置属性。
     */
    private final OperationLogProperties properties;
    /**
     * requestIdentityAccessor。
     */
    private final RequestIdentityAccessor requestIdentityAccessor;

    public OperationLogCommand enrich(OperationLogCommand command) {
        HttpServletRequest request = currentRequest();
        CurrentOperator currentOperator = resolveCurrentOperator();
        Map<String, Object> ext = new LinkedHashMap<>();
        if (command.getExt() != null) {
            ext.putAll(command.getExt());
        }

        return OperationLogCommand.builder()
                .appCode(firstNonBlank(command.getAppCode(), header(request, APP_CODE_HEADER), properties.getAppCode()))
                .appName(firstNonBlank(command.getAppName(), header(request, APP_NAME_HEADER), properties.getAppName()))
                .platformCode(firstNonBlank(command.getPlatformCode(), header(request, PLATFORM_CODE_HEADER), properties.getPlatformCode()))
                .platformName(firstNonBlank(command.getPlatformName(), header(request, PLATFORM_NAME_HEADER), properties.getPlatformName()))
                .module(command.getModule())
                .feature(command.getFeature())
                .operationType(command.getOperationType())
                .operationName(command.getOperationName())
                .operationDesc(command.getOperationDesc())
                .userId(firstNonBlank(command.getUserId(), currentOperator.userId()))
                .username(firstNonBlank(command.getUsername(), currentOperator.username()))
                .displayName(firstNonBlank(command.getDisplayName(), currentOperator.displayName()))
                .tenantId(firstNonBlank(command.getTenantId(), requestIdentityAccessor.resolveTenantId(request)))
                .requestMethod(firstNonBlank(command.getRequestMethod(), request == null ? null : request.getMethod()))
                .requestPath(firstNonBlank(command.getRequestPath(), request == null ? null : request.getRequestURI()))
                .requestQuery(command.getRequestQuery())
                .requestBodySummary(command.getRequestBodySummary())
                .responseSummary(command.getResponseSummary())
                .resultStatus(command.getResultStatus())
                .errorCode(command.getErrorCode())
                .errorMessageSummary(command.getErrorMessageSummary())
                .exceptionType(command.getExceptionType())
                .exceptionStackSummary(command.getExceptionStackSummary())
                .clientIp(firstNonBlank(command.getClientIp(), requestIdentityAccessor.resolveClientIp(request)))
                .userAgent(firstNonBlank(command.getUserAgent(), request == null ? null : request.getHeader("User-Agent")))
                .traceId(firstNonBlank(command.getTraceId(), resolveTraceId(request)))
                .requestId(firstNonBlank(command.getRequestId(), resolveRequestId(request)))
                .startTime(command.getStartTime())
                .endTime(command.getEndTime())
                .durationMs(command.getDurationMs())
                .sourceSystem(firstNonBlank(command.getSourceSystem(), header(request, SOURCE_SYSTEM_HEADER), properties.getSourceSystem()))
                .ext(ext)
                .build();
    }

    public OperationLogCommand createBaseCommand(OperationLog operationLog, ProceedingJoinPoint joinPoint) {
        Map<String, Object> ext = new LinkedHashMap<>();
        ext.put("declaringType", joinPoint.getSignature().getDeclaringTypeName());
        ext.put("methodName", joinPoint.getSignature().getName());
        return OperationLogCommand.builder()
                .appCode(operationLog.appCode())
                .appName(operationLog.appName())
                .platformCode(operationLog.platformCode())
                .platformName(operationLog.platformName())
                .module(operationLog.module())
                .feature(operationLog.feature())
                .operationType(operationLog.operationType())
                .operationName(operationLog.operationName())
                .operationDesc(operationLog.operationDesc())
                .sourceSystem(operationLog.sourceSystem())
                .ext(ext)
                .build();
    }

    public HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    /**
     * 解析链路追踪标识。
     */
    private String resolveTraceId(HttpServletRequest request) {
        String traceId = TraceContextHolder.getTraceId();
        if (StringUtils.hasText(traceId)) {
            return traceId;
        }
        Object requestTraceId = request == null ? null : request.getAttribute(TraceConstants.TRACE_ID_ATTRIBUTE);
        return requestTraceId == null ? null : String.valueOf(requestTraceId);
    }

    /**
     * 解析请求标识。
     */
    private String resolveRequestId(HttpServletRequest request) {
        String requestId = TraceContextHolder.getRequestId();
        if (StringUtils.hasText(requestId)) {
            return requestId;
        }
        Object currentRequestId = request == null ? null : request.getAttribute(TraceConstants.REQUEST_ID_ATTRIBUTE);
        return currentRequestId == null ? null : String.valueOf(currentRequestId);
    }

    /**
     * 解析当前登录人的基础信息。
     */
    private CurrentOperator resolveCurrentOperator() {
        try {
            if (StpUtil.isLogin()) {
                SaSession session = StpUtil.getTokenSession();
                return new CurrentOperator(
                        String.valueOf(StpUtil.getLoginIdAsLong()),
                        getSessionValue(session, LogSessionKeys.USERNAME),
                        getSessionValue(session, LogSessionKeys.DISPLAY_NAME)
                );
            }
        } catch (Exception ignored) {
        }

        try {
            if (StpMemberUtil.isLogin()) {
                SaSession session = StpMemberUtil.getStpLogic().getTokenSession();
                return new CurrentOperator(
                        String.valueOf(StpMemberUtil.getLoginIdAsLong()),
                        getSessionValue(session, LogSessionKeys.USERNAME),
                        getSessionValue(session, LogSessionKeys.DISPLAY_NAME)
                );
            }
        } catch (Exception ignored) {
        }

        return new CurrentOperator(null, null, null);
    }

    /**
     * 获取会话值。
     */
    private String getSessionValue(SaSession session, String key) {
        if (session == null) {
            return null;
        }
        Object value = session.get(key);
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 读取指定请求头并做去空白处理。
     */
    private String header(HttpServletRequest request, String headerName) {
        if (request == null || !StringUtils.hasText(headerName)) {
            return null;
        }
        String value = request.getHeader(headerName);
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    /**
     * 返回参数中的第一个非空白字符串。
     */
    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    /**
     * 当前登录人的简化快照。
     */
    private record CurrentOperator(String userId, String username, String displayName) {
    }
}
