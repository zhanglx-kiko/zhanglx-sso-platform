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
 * 会员忘记密码请求对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "MemberForgotPasswordDTO", description = "会员忘记密码参数")
public class MemberForgotPasswordDTO implements Serializable {

    /**
     * 手机号。
     */
    @NotBlank(message = "{member.phone.cannot.be.blank}")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "{member.phone.invalid}")
    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    private String phoneNumber;

    /**
     * 新密码。
     */
    @NotBlank(message = "{member.new.password.cannot.be.blank}")
    @Size(min = 6, max = 32, message = "{member.password.length.invalid}")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$", message = "{member.password.pattern.invalid}")
    @Schema(description = "新密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "Zhanglx123")
    @XssPolicy(XssPolicyMode.NONE)
    private String newPassword;

    /**
     * 6 位短信验证码。
     */
    @NotBlank(message = "{member.verification.code.cannot.be.blank}")
    @Pattern(regexp = "^\\d{6}$", message = "{member.verification.code.length.invalid}")
    @Schema(description = "6 位短信验证码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    private String verificationCode;
}