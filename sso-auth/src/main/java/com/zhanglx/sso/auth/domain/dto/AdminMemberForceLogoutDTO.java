package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.core.config.StringToLongDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

/**
 * 后台强制会员下线请求。
 */
@Data
@Schema(name = "AdminMemberForceLogoutDTO", description = "后台强制会员下线请求")
public class AdminMemberForceLogoutDTO {

    /**
     * 会员 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(description = "会员ID")
    private Long memberId;

    /**
     * 操作原因。
     */
    @NotBlank(message = "{member.manage.reason.cannot.be.blank}")
    @Size(max = 255, message = "{member.manage.reason.length.invalid}")
    @Schema(description = "操作原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;

    /**
     * 备注。
     */
    @Size(max = 255, message = "{member.manage.remark.length.invalid}")
    @Schema(description = "备注")
    private String remark;
}