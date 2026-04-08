package com.zhanglx.sso.auth.domain.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 登录日志落库命令。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthLoginLogRecordCommand {

    private Long userId;
    private String username;
    private String displayName;
    private String eventType;
    private String loginResult;
    private String failReason;
    private String loginIp;
    private String userAgent;
    private String deviceType;
    private String traceId;
    private String requestId;
    private String clientType;
    private String appCode;
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
    private String extJson;
}
