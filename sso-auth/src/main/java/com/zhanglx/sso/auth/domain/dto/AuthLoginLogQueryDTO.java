package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.core.config.StringToLocalDateTimeDeserializer;
import com.zhanglx.sso.core.config.StringToLongDeserializer;
import com.zhanglx.sso.core.domain.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;

/**
 * 登录日志查询条件。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "AuthLoginLogQueryDTO", description = "登录日志查询条件")
public class AuthLoginLogQueryDTO extends PageQuery {

    /**
     * 用户ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户名。
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 事件类型，LOGIN / LOGOUT。
     */
    @Schema(description = "事件类型，LOGIN / LOGOUT")
    private String eventType;

    /**
     * 登录结果，SUCCESS / FAILURE。
     */
    @Schema(description = "登录结果，SUCCESS / FAILURE")
    private String loginResult;

    /**
     * 登录 IP。
     */
    @Schema(description = "登录 IP")
    private String loginIp;

    /**
     * 开始时间。
     */
    @JsonDeserialize(using = StringToLocalDateTimeDeserializer.class)
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    /**
     * 结束时间。
     */
    @JsonDeserialize(using = StringToLocalDateTimeDeserializer.class)
    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}
