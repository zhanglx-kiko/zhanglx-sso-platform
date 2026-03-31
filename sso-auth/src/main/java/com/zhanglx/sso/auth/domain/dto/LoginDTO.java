package com.zhanglx.sso.auth.domain.dto;


import com.zhanglx.sso.core.domain.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/10 20:48
 * @ClassName: LoginDTO
 * @Description:
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "LoginDTO", description = "登录对象")
public class LoginDTO extends BaseDTO {

    /**
     * 用户名
     */
    @Schema(description = "用户名", name = "username", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private String username;

    /**
     * 密码
     */
    @Schema(description = "密码", name = "password", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private String password;

    /**
     * 登录设备标识 (可选，例如: "PC", "APP", "H5")
     * 用于支持多端登录识别
     */
    @Schema(description = "登录设备标识", name = "device", example = "PC,APP,H5", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private String device;

}
