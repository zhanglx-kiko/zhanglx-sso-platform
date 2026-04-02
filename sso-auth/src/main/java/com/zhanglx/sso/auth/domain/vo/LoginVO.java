package com.zhanglx.sso.auth.domain.vo;

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;
import com.zhanglx.sso.core.config.StringToLongDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/10 21:12
 * @ClassName: LoginVO
 * @Description:
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
@Schema(name = "LoginVO", description = "登录对象")
public class LoginVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户主键ID (String类型，防止前端精度丢失)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(description = "用户名", name = "username", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    /**
     * 用户名
     */
    @Schema(description = "用户名", name = "username", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private String username;

    /**
     * 昵称
     */
    @Schema(description = "昵称", name = "nickname", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private String nickname;

    /**
     * 头像
     */
    @Schema(description = "头像", name = "avatar", example = "", type = "String", requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private String avatar;

    /**
     * 部门ID (String类型)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(description = "部门ID", name = "deptId", example = "", type = "String", requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private Long deptId;

    /**
     * Token 名称 (例如: satoken)
     */
    @Schema(description = "Token 名称", name = "tokenName", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private String tokenName;

    /**
     * Token 值
     */
    @Schema(description = "Token 值", name = "tokenValue", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private String tokenValue;

}
