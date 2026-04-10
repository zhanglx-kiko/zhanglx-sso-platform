package com.zhanglx.sso.auth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 忘记密码验证码校验请求对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ForgotPasswordVerificationCodeVerifyDTO", description = "后台忘记密码验证码校验参数")
public class ForgotPasswordVerificationCodeVerifyDTO {

    /**
     * 账号。
     */
    @NotBlank(message = "{user.username.cannot.be.blank}")
    @Schema(description = "账号", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
    private String username;

    /**
     * 6 位短信验证码。
     */
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "{sms.verification.code.length.invalid}")
    @Schema(description = "6 位短信验证码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    private String verificationCode;
}
