package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.auth.enums.MemberManageActionTypeEnum;
import com.zhanglx.sso.auth.enums.UserStatusEnum;
import com.zhanglx.sso.core.config.StringToLocalDateTimeDeserializer;
import com.zhanglx.sso.core.config.StringToLongDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;

/**
 * 后台会员状态控制请求。
 */
@Data
@Schema(name = "AdminMemberStatusUpdateDTO", description = "后台会员状态控制请求")
public class AdminMemberStatusUpdateDTO {

    /**
     * 会员 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(description = "会员ID")
    private Long memberId;

    /**
     * 管理动作类型。
     */
    @Schema(description = "管理动作类型")
    private MemberManageActionTypeEnum actionType;

    /**
     * 目标状态。
     */
    @Schema(description = "目标状态")
    private UserStatusEnum targetStatus;

    /**
     * 操作原因。
     */
    @NotBlank(message = "{member.manage.reason.cannot.be.blank}")
    @Size(max = 255, message = "{member.manage.reason.length.invalid}")
    @Schema(description = "操作原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;

    /**
     * 到期时间。
     */
    @JsonDeserialize(using = StringToLocalDateTimeDeserializer.class)
    @Schema(description = "到期时间")
    private LocalDateTime expireTime;

    /**
     * 备注。
     */
    @Size(max = 255, message = "{member.manage.remark.length.invalid}")
    @Schema(description = "备注")
    private String remark;
}