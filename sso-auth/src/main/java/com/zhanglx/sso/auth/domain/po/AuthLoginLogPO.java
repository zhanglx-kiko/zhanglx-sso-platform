package com.zhanglx.sso.auth.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 登录日志实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_auth_login_log")
public class AuthLoginLogPO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private String username;

    @TableField("display_name")
    private String displayName;

    @TableField("event_type")
    private String eventType;

    @TableField("login_result")
    private String loginResult;

    @TableField("fail_reason")
    private String failReason;

    @TableField("login_ip")
    private String loginIp;

    @TableField("user_agent")
    private String userAgent;

    @TableField("device_type")
    private String deviceType;

    @TableField("trace_id")
    private String traceId;

    @TableField("request_id")
    private String requestId;

    @TableField("client_type")
    private String clientType;

    @TableField("app_code")
    private String appCode;

    @TableField("login_time")
    private LocalDateTime loginTime;

    @TableField("logout_time")
    private LocalDateTime logoutTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("ext_json")
    private String extJson;
}
