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

    /**
     * ID。
     */
    /**
     * 用户ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    /**
     * 用户名。
     */

    private String username;

    /**
     * 显示名称。
     */
    @TableField("display_name")
    private String displayName;

    /**
     * 事件类型。
     */
    @TableField("event_type")
    private String eventType;

    /**
     * 登录结果。
     */
    @TableField("login_result")
    private String loginResult;

    /**
     * 失败原因。
     */
    @TableField("fail_reason")
    private String failReason;

    /**
     * 登录IP。
     */
    @TableField("login_ip")
    private String loginIp;

    /**
     * 用户代理。
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 设备类型。
     */
    @TableField("device_type")
    private String deviceType;

    /**
     * 链路追踪ID。
     */
    @TableField("trace_id")
    private String traceId;

    /**
     * 请求ID。
     */
    @TableField("request_id")
    private String requestId;

    /**
     * 客户端类型。
     */
    @TableField("client_type")
    private String clientType;

    /**
     * 应用编码。
     */
    @TableField("app_code")
    private String appCode;

    /**
     * 登录时间。
     */
    @TableField("login_time")
    private LocalDateTime loginTime;

    /**
     * 登出时间。
     */
    @TableField("logout_time")
    private LocalDateTime logoutTime;

    /**
     * createTime。
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 扩展JSON。
     */
    @TableField("ext_json")
    private String extJson;
}
