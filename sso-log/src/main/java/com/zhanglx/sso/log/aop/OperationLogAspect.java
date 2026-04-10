package com.zhanglx.sso.log.aop;

import com.zhanglx.sso.common.result.Result;
import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.log.annotation.OperationLog;
import com.zhanglx.sso.log.domain.command.OperationLogCommand;
import com.zhanglx.sso.log.service.OperationLogClient;
import com.zhanglx.sso.log.support.OperationLogContextResolver;
import com.zhanglx.sso.log.support.OperationLogSummarySanitizer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 操作日志切面。
 */
@Slf4j
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 100)
@RequiredArgsConstructor
public class OperationLogAspect {
    /**
     * 操作日志上下文解析器。
     */
    private final OperationLogContextResolver contextResolver;

    /**
     * 日志摘要脱敏处理器。
     */
    private final OperationLogSummarySanitizer summarySanitizer;

    /**
     * 操作日志记录客户端。
     */
    private final OperationLogClient operationLogClient;

    /**
     * 环绕拦截标注了操作日志注解的方法。
     */
    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        HttpServletRequest request = contextResolver.currentRequest();
        LocalDateTime startTime = LocalDateTime.now();
        long startNanoTime = System.nanoTime();
        Object result = null;
        Throwable throwable = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            throwable = ex;
            throw ex;
        } finally {
            try {
                Map<String, Object> ext = new LinkedHashMap<>();
                OperationLogCommand baseCommand = contextResolver.createBaseCommand(operationLog, joinPoint);
                if (baseCommand.getExt() != null) {
                    ext.putAll(baseCommand.getExt());
                }
                ext.put("resultClass", result == null ? null : result.getClass().getSimpleName());

                OperationLogCommand command = OperationLogCommand.builder()
                        .appCode(baseCommand.getAppCode())
                        .appName(baseCommand.getAppName())
                        .platformCode(baseCommand.getPlatformCode())
                        .platformName(baseCommand.getPlatformName())
                        .module(baseCommand.getModule())
                        .feature(baseCommand.getFeature())
                        .operationType(baseCommand.getOperationType())
                        .operationName(baseCommand.getOperationName())
                        .operationDesc(baseCommand.getOperationDesc())
                        .requestMethod(request == null ? null : request.getMethod())
                        .requestPath(request == null ? null : request.getRequestURI())
                        .requestQuery(summarySanitizer.summarizeQuery(request == null ? null : request.getQueryString()))
                        .requestBodySummary(operationLog.includeRequestBody() ? summarySanitizer.summarizeRequest(joinPoint.getSignature(), joinPoint.getArgs()) : null)
                        .responseSummary(summarySanitizer.summarizeResponse(result, operationLog.includeResponseBody()))
                        .resultStatus(resolveResultStatus(result, throwable))
                        .errorCode(resolveErrorCode(result, throwable))
                        .errorMessageSummary(summarySanitizer.summarizeErrorMessage(throwable))
                        .exceptionType(throwable == null ? null : throwable.getClass().getName())
                        .exceptionStackSummary(summarySanitizer.summarizeStackTrace(throwable))
                        .startTime(startTime)
                        .endTime(LocalDateTime.now())
                        .durationMs((System.nanoTime() - startNanoTime) / 1_000_000L)
                        .sourceSystem(baseCommand.getSourceSystem())
                        .ext(ext)
                        .build();
                operationLogClient.record(command);
            } catch (Exception logException) {
                // 日志链路属于旁路能力，采集失败时只记录内部告警，不影响主业务结果。
                log.warn("操作日志切面采集失败，已忽略主流程影响: {}", logException.getMessage());
            }
        }
    }

    /**
     * 根据响应结果和异常信息推导操作执行状态。
     */
    private String resolveResultStatus(Object result, Throwable throwable) {
        if (throwable != null) {
            return "FAILURE";
        }
        if (result instanceof Result<?> response && response.getCode() != null && response.getCode() >= 400) {
            return "FAILURE";
        }
        return "SUCCESS";
    }

    /**
     * 从响应结果或异常中提取错误码。
     */
    private String resolveErrorCode(Object result, Throwable throwable) {
        if (throwable instanceof BusinessException businessException) {
            return businessException.getCode() == null ? null : String.valueOf(businessException.getCode());
        }
        if (result instanceof Result<?> response && response.getCode() != null && response.getCode() >= 400) {
            return String.valueOf(response.getCode());
        }
        return null;
    }
}
