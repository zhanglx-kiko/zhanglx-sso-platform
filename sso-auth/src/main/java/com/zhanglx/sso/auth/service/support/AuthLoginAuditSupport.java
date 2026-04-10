package com.zhanglx.sso.auth.service.support;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.zhanglx.sso.auth.domain.command.AuthLoginLogRecordCommand;
import com.zhanglx.sso.auth.service.AuthLoginLogService;
import com.zhanglx.sso.common.trace.TraceConstants;
import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.core.trace.TraceContextHolder;
import com.zhanglx.sso.core.utils.satoken.StpMemberUtil;
import com.zhanglx.sso.log.constants.LogSessionKeys;
import com.zhanglx.sso.log.support.OperationLogContextResolver;
import com.zhanglx.sso.log.support.OperationLogSummarySanitizer;
import com.zhanglx.sso.web.support.RequestIdentityAccessor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 登录日志辅助组件。
 */
@Component
@RequiredArgsConstructor
public class AuthLoginAuditSupport {

    public static final String EVENT_TYPE_LOGIN = "LOGIN";
    public static final String EVENT_TYPE_LOGOUT = "LOGOUT";
    public static final String RESULT_SUCCESS = "SUCCESS";
    public static final String RESULT_FAILURE = "FAILURE";
    public static final String CLIENT_TYPE_SYS_PASSWORD = "SYS_PASSWORD";
    public static final String CLIENT_TYPE_MEMBER_PASSWORD = "MEMBER_PASSWORD";
    public static final String CLIENT_TYPE_SYS_WECHAT = "SYS_WECHAT";
    public static final String CLIENT_TYPE_MEMBER_WECHAT = "MEMBER_WECHAT";
    /**
     * 登录日志服务。
     */
    private final AuthLoginLogService authLoginLogService;
    /**
     * 请求标识访问器。
     */
    private final RequestIdentityAccessor requestIdentityAccessor;
    /**
     * 摘要脱敏器。
     */
    private final OperationLogSummarySanitizer summarySanitizer;
    /**
     * 对象映射器。
     */
    private final ObjectMapper objectMapper;

    public void storeAdminSession(String username, String displayName, String clientType) {
        try {
            SaSession session = StpUtil.getTokenSession();
            session.set(LogSessionKeys.USERNAME, username);
            session.set(LogSessionKeys.DISPLAY_NAME, displayName);
            session.set(LogSessionKeys.CLIENT_TYPE, clientType);
        } catch (Exception ignored) {
        }
    }

    public void storeMemberSession(String username, String displayName, String clientType) {
        try {
            SaSession session = StpMemberUtil.getStpLogic().getTokenSession();
            session.set(LogSessionKeys.USERNAME, username);
            session.set(LogSessionKeys.DISPLAY_NAME, displayName);
            session.set(LogSessionKeys.CLIENT_TYPE, clientType);
        } catch (Exception ignored) {
        }
    }

    public void recordLoginSuccess(Long userId, String username, String displayName, String deviceType, String clientType) {
        authLoginLogService.recordAsync(baseCommand(userId, username, displayName, deviceType, clientType)
                .eventType(EVENT_TYPE_LOGIN)
                .loginResult(RESULT_SUCCESS)
                .loginTime(LocalDateTime.now())
                .build());
    }

    public void recordLoginFailure(String username, String displayName, String deviceType, String clientType, Throwable throwable) {
        authLoginLogService.recordAsync(baseCommand(null, username, displayName, deviceType, clientType)
                .eventType(EVENT_TYPE_LOGIN)
                .loginResult(RESULT_FAILURE)
                .failReason(resolveFailureReason(throwable))
                .loginTime(LocalDateTime.now())
                .build());
    }

    public void recordLogout(Long userId, String username, String displayName, String deviceType, String clientType) {
        authLoginLogService.recordAsync(baseCommand(userId, username, displayName, deviceType, clientType)
                .eventType(EVENT_TYPE_LOGOUT)
                .loginResult(RESULT_SUCCESS)
                .logoutTime(LocalDateTime.now())
                .build());
    }

    public SessionSnapshot currentAdminSnapshot() {
        return buildSnapshot(true);
    }

    public SessionSnapshot currentMemberSnapshot() {
        return buildSnapshot(false);
    }

    private AuthLoginLogRecordCommand.AuthLoginLogRecordCommandBuilder baseCommand(Long userId,
                                                                                   String username,
                                                                                   String displayName,
                                                                                   String deviceType,
                                                                                   String clientType) {
        HttpServletRequest request = currentRequest();
        return AuthLoginLogRecordCommand.builder()
                .userId(userId)
                .username(username)
                .displayName(displayName)
                .loginIp(requestIdentityAccessor.resolveClientIp(request))
                .userAgent(summarySanitizer.truncateUserAgent(request == null ? null : request.getHeader("User-Agent")))
                .deviceType(deviceType)
                .traceId(resolveTraceId(request))
                .requestId(resolveRequestId(request))
                .clientType(clientType)
                .appCode(resolveAppCode(request))
                .extJson(buildExtJson(request, clientType));
    }

    /**
     * 构建登录审计快照。
     */
    private SessionSnapshot buildSnapshot(boolean admin) {
        try {
            if (admin && StpUtil.isLogin()) {
                SaSession session = StpUtil.getTokenSession();
                return new SessionSnapshot(
                        StpUtil.getLoginIdAsLong(),
                        stringValue(session.get(LogSessionKeys.USERNAME)),
                        stringValue(session.get(LogSessionKeys.DISPLAY_NAME)),
                        stringValue(session.get(LogSessionKeys.CLIENT_TYPE))
                );
            }
            if (!admin && StpMemberUtil.isLogin()) {
                SaSession session = StpMemberUtil.getStpLogic().getTokenSession();
                return new SessionSnapshot(
                        StpMemberUtil.getLoginIdAsLong(),
                        stringValue(session.get(LogSessionKeys.USERNAME)),
                        stringValue(session.get(LogSessionKeys.DISPLAY_NAME)),
                        stringValue(session.get(LogSessionKeys.CLIENT_TYPE))
                );
            }
        } catch (Exception ignored) {
        }
        return new SessionSnapshot(null, null, null, null);
    }

    /**
     * 构建扩展信息 序列化文本。
     */
    private String buildExtJson(HttpServletRequest request, String clientType) {
        Map<String, Object> ext = new LinkedHashMap<>();
        ext.put("requestUri", request == null ? null : request.getRequestURI());
        ext.put("clientType", clientType);
        try {
            return objectMapper.writeValueAsString(ext);
        } catch (Exception e) {
            return "{\"clientType\":\"" + clientType + "\"}";
        }
    }

    /**
     * 提炼失败原因，便于日志和异常输出。
     */
    private String resolveFailureReason(Throwable throwable) {
        if (throwable instanceof BusinessException businessException) {
            return StringUtils.hasText(businessException.getMessage())
                    ? businessException.getMessage()
                    : businessException.getMessageKey();
        }
        return throwable == null ? null : throwable.getMessage();
    }

    /**
     * 解析应用编码。
     */
    private String resolveAppCode(HttpServletRequest request) {
        if (request == null) {
            return "sso";
        }
        String appCode = request.getHeader(OperationLogContextResolver.APP_CODE_HEADER);
        return StringUtils.hasText(appCode) ? appCode.trim() : "sso";
    }

    /**
     * 解析请求链路追踪标识。
     */
    private String resolveTraceId(HttpServletRequest request) {
        String traceId = TraceContextHolder.getTraceId();
        if (StringUtils.hasText(traceId)) {
            return traceId;
        }
        Object currentTraceId = request == null ? null : request.getAttribute(TraceConstants.TRACE_ID_ATTRIBUTE);
        return currentTraceId == null ? null : String.valueOf(currentTraceId);
    }

    /**
     * 解析请求编号。
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
     * 获取当前线程中的请求对象。
     */
    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    /**
     * 将对象安全转换为字符串。
     */
    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    public record SessionSnapshot(Long userId, String username, String displayName, String clientType) {
    }
}