package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.xss.annotation.XssPolicy;
import com.zhanglx.sso.xss.support.XssPolicyMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 作者：Zhang L X
 * 创建时间：2026/4/2 10:00
 * 类名：ForgotPasswordDTO
 * 说明：忘记密码请求参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ForgotPasswordDTO", description = "忘记密码请求参数")
public class ForgotPasswordDTO implements Serializable {

    /**
     * 账号。
     */
    @NotBlank(message = "账号不能为空")
    @Schema(description = "账号", name = "username", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    /**
     * 新密码。
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "{user.password.length.invalid}")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$", message = "{user.password.pattern.invalid}")
    @Schema(description = "新密码", name = "newPassword", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED)
    @XssPolicy(XssPolicyMode.NONE)
    private String newPassword;

    /**
     * 6 位短信验证码。
     */
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "{sms.verification.code.length.invalid}")
    @Schema(description = "6 位短信验证码", name = "verificationCode", example = "123456", type = "String", requiredMode = Schema.RequiredMode.REQUIRED)
    private String verificationCode;
}