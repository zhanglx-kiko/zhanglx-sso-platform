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
 * 登录日志展示对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AuthLoginLogVO", description = "登录日志")
public class AuthLoginLogVO {

    /**
     * ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long id;

    /**
     * 用户ID。
     */
    /**
     * 用户名。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long userId;

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
     * 登录IP。
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
     * 链路追踪ID。
     */

    private String traceId;
    /**
     * 请求ID。
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
     * 登出时间。
     */

    private LocalDateTime logoutTime;
    /**
     * createTime。
     */

    private LocalDateTime createTime;
    /**
     * 扩展JSON。
     */

    private String extJson;
}
