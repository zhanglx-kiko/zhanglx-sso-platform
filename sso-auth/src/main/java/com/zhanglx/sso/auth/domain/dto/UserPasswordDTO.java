package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.core.config.StringToLongDeserializer;
import com.zhanglx.sso.xss.annotation.XssPolicy;
import com.zhanglx.sso.xss.support.XssPolicyMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.io.Serializable;

/**
 * 作者：Zhang L X
 * 创建时间：2026/2/12 16:46
 * 类名：UserPasswordDTO
 * 说明：系统用户修改密码请求对象。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Schema(name = "UserPasswordDTO", description = "用户修改密码对象")
public class UserPasswordDTO implements Serializable {

    /**
     * 用户标识。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(description = "用户ID", name = "userId", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private Long userId;

    /**
     * 旧密码。
     */
    @NotBlank(message = "旧密码不能为空")
    @Schema(description = "旧密码", name = "oldPassword", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    @XssPolicy(XssPolicyMode.NONE)
    private String oldPassword;

    /**
     * 新密码。
     */
    @NotBlank(message = "新密码不能为空")
    @Schema(description = "新密码", name = "newPassword", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    @XssPolicy(XssPolicyMode.NONE)
    private String newPassword;

}