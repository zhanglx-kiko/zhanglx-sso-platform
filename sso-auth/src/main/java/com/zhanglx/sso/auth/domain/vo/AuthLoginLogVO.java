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

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
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

    private LocalDateTime createTime;

    private String extJson;
}
