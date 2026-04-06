package com.zhanglx.sso.auth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "MemberUpdateDTO", description = "会员资料更新参数")
public class MemberUpdateDTO {

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "{member.phone.invalid}")
    @Schema(
            description = "手机号字段仅保留兼容，实际修改请调用绑定手机号接口",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "13800138000"
    )
    private String phoneNumber;
}
