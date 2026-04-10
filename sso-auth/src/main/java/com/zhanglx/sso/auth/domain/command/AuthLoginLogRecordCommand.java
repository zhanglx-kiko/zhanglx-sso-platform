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

    /**
     * 用户标识。
     */
    private Long userId;
    /**
     * 用户名。
     */
    private String username;
    /**
     * 显示名称。
     */
    private String displayName;
    /**
     * 事件类型。
     */
    private String eventType;
    /**
     * 登录结果。
     */
    private String loginResult;
    /**
     * 失败原因。
     */
    private String failReason;
    /**
     * 登录地址。
     */
    private String loginIp;
    /**
     * 用户代理。
     */
    private String userAgent;
    /**
     * 设备类型。
     */
    private String deviceType;
    /**
     * 链路跟踪标识。
     */
    private String traceId;
    /**
     * 请求标识。
     */
    private String requestId;
    /**
     * 客户端类型。
     */
    private String clientType;
    /**
     * 应用编码。
     */
    private String appCode;
    /**
     * 登录时间。
     */
    private LocalDateTime loginTime;
    /**
     * 退出时间。
     */
    private LocalDateTime logoutTime;
    /**
     * 扩展信息序列化内容。
     */
    private String extJson;
}
