package com.zhanglx.sso.auth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "MemberBindPhoneDTO", description = "会员绑定手机号参数")
public class MemberBindPhoneDTO {

    @NotBlank(message = "{member.phone.cannot.be.blank}")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "{member.phone.invalid}")
    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    private String phoneNumber;

    @NotBlank(message = "{member.verification.code.cannot.be.blank}")
    @Size(min = 4, max = 6, message = "{member.verification.code.length.invalid}")
    @Schema(description = "验证码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    private String verificationCode;
}
