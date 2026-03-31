package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.auth.domain.vo.PermissionVO;
import com.zhanglx.sso.core.domain.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/18 09:30
 * @ClassName: RoleDTO
 * @Description:
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "RoleDTO", description = "用户对象")
public class RoleDTO extends BaseDTO {

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    @Schema(description = "角色名称", name = "roleName", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private String roleName;

    /**
     * 角色编码
     */
    @Schema(description = "角色编码", name = "roleCode", example = "admin", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private String roleCode;

    /**
     * 角色类型
     */
    @Schema(description = "角色类型", name = "roleType", example = "角色类型", type = "String", requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private String roleType;

    /**
     * 是否内置
     */
    @Schema(description = "是否内置:1-内置,0-非内置", name = "buildIn", example = "0", type = "Integer", hidden = true, defaultValue = "0", allowableValues = {"1", "0"}, requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private Integer buildIn;

    /**
     * 权限项列表
     */
    @Schema(description = "权限项列表", name = "rolePermissions", example = "", type = "List<PermissionVO>", requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private List<PermissionVO> rolePermissions;

    /**
     * 备注
     */
    @Schema(description = "备注", name = "remark", example = "备注", type = "String", requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private String remark;

}
