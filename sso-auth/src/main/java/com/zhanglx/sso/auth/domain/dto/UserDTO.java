package com.zhanglx.sso.auth.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zhanglx.sso.auth.enums.UserStatusEnum;
import com.zhanglx.sso.xss.annotation.XssPolicy;
import com.zhanglx.sso.xss.support.XssPolicyMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "UserDTO", description = "Admin user DTO")
public class UserDTO extends UserBaseDTO {

    /**
     * 账号。
     */
    @NotBlank(message = "账号不能为空")
    @Schema(description = "Username", name = "username", example = "", type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_WRITE)
    private String username;

    // 添加了@JsonIgnore后MapStruct会自动过滤字段，不需要额外在转换方法中加@Mapping(target = "password", ignore = true)
    @JsonIgnore
    @Schema(description = "Password", name = "password", example = "", type = "String",
            hidden = true,
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY)
    @XssPolicy(XssPolicyMode.NONE)
    private String password;

    /**
     * 微信用户唯一标识。
     */
    @JsonIgnore
    @Schema(description = "Wechat open id", name = "openId", example = "", type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY)
    private String openId;

    @Schema(description = "Department name", name = "deptName", example = "", type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY)
    private String deptName;

    @Schema(description = "Status", name = "status", example = "0 disabled, 1 enabled",
            defaultValue = "1", allowableValues = {"0", "1"}, type = "Integer",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_WRITE)
    private UserStatusEnum status;
}