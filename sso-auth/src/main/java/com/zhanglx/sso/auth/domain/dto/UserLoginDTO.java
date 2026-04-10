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
 * 作者：Zhang L X
 * 创建时间：2026/2/10 20:48
 * 类名：用户登录参数
 * 说明：系统用户登录请求对象。
 */

/**
 * UserLogin数据传输对象。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "UserLoginDTO", description = "登录对象")
public class UserLoginDTO extends BaseDTO {

    /**
     * 用户名
     */
    @NotBlank(message = "账号不能为空")
    @Schema(description = "username", name = "username", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", name = "password", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    @XssPolicy(XssPolicyMode.NONE)
    private String password;

    /**
     * 登录设备标识 (可选，例如: "PC", "APP", "H5")
     * 用于支持多端登录识别
     */
    @Schema(description = "登录设备标识", name = "device", example = "PC,APP,H5", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private String device;

}