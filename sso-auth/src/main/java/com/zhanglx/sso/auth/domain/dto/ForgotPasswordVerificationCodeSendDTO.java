package com.zhanglx.sso.auth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 忘记密码验证码发送请求对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ForgotPasswordVerificationCodeSendDTO", description = "后台忘记密码发送验证码参数")
public class ForgotPasswordVerificationCodeSendDTO {

    /**
     * 账号。
     */
    @NotBlank(message = "{user.username.cannot.be.blank}")
    @Schema(description = "账号", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
    private String username;
}