package com.zhanglx.sso.auth.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.zhanglx.sso.core.config.StringToLongDeserializer;
import com.zhanglx.sso.core.domain.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/11 16:31
 * @ClassName: UserDTO
 * @Description:
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "UserDTO", description = "用户对象")
public class UserDTO extends BaseDTO {

    @NotBlank(message = "姓名不能为空")
    @Schema(description = "用户名", name = "username", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private String username;

    // 添加了@JsonIgnore后MapStruct会自动过滤字段，不需要额外在转换方法中加@Mapping(target = "password", ignore = true)
    @JsonIgnore
    @Schema(description = "密码", name = "password", example = "", type = "String", hidden = true, requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private String password;

    @Schema(description = "名称", name = "nickname", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private String nickname;

    @Schema(description = "头像", name = "avatar", example = "", type = "String", requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private String avatar;

    @JsonIgnore
    @Schema(description = "wx用户唯一标识", name = "openId", example = "", type = "String", requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private String openId;

    /**
     * 是否允许并发登录：0-禁止(会顶号)，1-允许(默认)
     */
    @Schema(description = "是否允许并发登录", name = "allowConcurrentLogin", example = "0", type = "Integer", requiredMode = Schema.RequiredMode.NOT_REQUIRED, defaultValue = "1", allowableValues = {"0", "1"}, accessMode = Schema.AccessMode.READ_WRITE)
    private Integer allowConcurrentLogin;

    /**
     * 部门ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(description = "部门ID", name = "deptId", example = "", type = "String", requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private Long deptId;

    @Schema(description = "状态", name = "status", example = "0禁用, 1正常", defaultValue = "1", allowableValues = {"0", "1"}, type = "Integer", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private Integer status;

}
