package com.zhanglx.sso.auth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: Zhang L X
 * @Create: 2026/4/2 10:00
 * @ClassName: ForgotPasswordDTO
 * @Description: 忘记密码请求参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ForgotPasswordDTO", description = "忘记密码请求参数")
public class ForgotPasswordDTO implements Serializable {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", name = "username", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "新密码不能为空")
    @Schema(description = "新密码", name = "newPassword", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码", name = "verificationCode", example = "123456", type = "String", requiredMode = Schema.RequiredMode.REQUIRED)
    private String verificationCode;
}
