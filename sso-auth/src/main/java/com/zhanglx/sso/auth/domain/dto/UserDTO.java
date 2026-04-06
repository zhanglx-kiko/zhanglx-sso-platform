package com.zhanglx.sso.auth.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class UserDTO extends UserBaseDTO {

    @NotBlank(message = "账号不能为空")
    @Schema(description = "账号", name = "username", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private String username;

    // 添加了@JsonIgnore后MapStruct会自动过滤字段，不需要额外在转换方法中加@Mapping(target = "password", ignore = true)
    @JsonIgnore
    @Schema(description = "密码", name = "password", example = "", type = "String", hidden = true, requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private String password;

    @JsonIgnore
    @Schema(description = "wx用户唯一标识", name = "openId", example = "", type = "String", requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private String openId;

    @JsonIgnore
    @Schema(description = "部门名称", name = "deptName", example = "", type = "String", requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private String deptName;

    @Schema(description = "状态", name = "status", example = "0禁用, 1正常", defaultValue = "1", allowableValues = {"0", "1"}, type = "Integer", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private Integer status;

}
