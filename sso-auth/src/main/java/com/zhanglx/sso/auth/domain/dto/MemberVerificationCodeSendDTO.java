package com.zhanglx.sso.auth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 会员验证码发送请求对象。
 */


@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "MemberVerificationCodeSendDTO", description = "会员验证码发送参数")
public class MemberVerificationCodeSendDTO {

    @NotBlank(message = "{member.phone.cannot.be.blank}")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "{member.phone.invalid}")
    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    private String phoneNumber;

    @NotBlank(message = "{member.verification.scene.cannot.be.blank}")
    @Schema(
            description = "验证码场景，可选值为 REGISTER、CHANGE_BOUND_PHONE、FORGOT_PASSWORD、BIND_PHONE、VERIFY_BIND_PHONE",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "REGISTER"
    )
    private String scene;
}