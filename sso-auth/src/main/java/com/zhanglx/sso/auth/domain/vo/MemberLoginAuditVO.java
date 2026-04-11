package com.zhanglx.sso.auth.domain.vo;

import com.zhanglx.sso.core.config.StringToLongDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;

/**
 * 会员登录审计展示对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "MemberLoginAuditVO", description = "会员登录审计展示对象")
public class MemberLoginAuditVO {

    /**
     * 日志 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long id;

    /**
     * 会员 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long userId;

    /**
     * 登录账号。
     */
    private String username;

    /**
     * 展示名称。
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
     * 登录 IP。
     */
    private String loginIp;

    /**
     * 设备类型。
     */
    private String deviceType;

    /**
     * 客户端类型。
     */
    private String clientType;

    /**
     * 登录时间。
     */
    private LocalDateTime loginTime;

    /**
     * 登出时间。
     */
    private LocalDateTime logoutTime;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
}