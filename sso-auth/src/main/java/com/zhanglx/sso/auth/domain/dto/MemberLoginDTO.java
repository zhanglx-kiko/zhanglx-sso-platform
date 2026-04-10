package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.core.domain.dto.BaseDTO;
import com.zhanglx.sso.xss.annotation.XssPolicy;
import com.zhanglx.sso.xss.support.XssPolicyMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 会员登录请求对象。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "MemberLoginDTO", description = "会员登录参数")
public class MemberLoginDTO extends BaseDTO {

    /**
     * 手机号。
     */
    @NotBlank(message = "{member.phone.cannot.be.blank}")
    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    private String phoneNumber;

    /**
     * 密码。
     */
    @NotBlank(message = "{member.password.cannot.be.blank}")
    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "Zhanglx123")
    @XssPolicy(XssPolicyMode.NONE)
    private String password;

    /**
     * 登录设备标识。
     */
    @Schema(description = "登录设备标识", example = "H5", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String device;
}
