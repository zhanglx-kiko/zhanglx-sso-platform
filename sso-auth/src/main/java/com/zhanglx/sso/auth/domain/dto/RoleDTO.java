package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.auth.domain.vo.PermissionVO;
import com.zhanglx.sso.auth.enums.DataScopeEnum;
import com.zhanglx.sso.auth.enums.EnableStatusEnum;
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
 * 角色数据传输对象。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "RoleDTO", description = "角色对象")
public class RoleDTO extends BaseDTO {

    /**
     * 角色名称。
     */
    @NotBlank(message = "{role.name.cannot.be.blank}")
    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roleName;

    /**
     * 角色编码。
     */
    @NotBlank(message = "{role.code.cannot.be.blank}")
    @Schema(description = "角色编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
    private String roleCode;

    /**
     * 应用编码。
     */
    @Schema(description = "应用编码", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "sso")
    private String appCode;

    /**
     * 数据范围。
     */
    @Schema(description = "数据范围", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1")
    private DataScopeEnum dataScope;

    /**
     * 状态。
     */
    @Schema(description = "状态", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "1")
    private EnableStatusEnum status;

    /**
     * 权限项列表。
     */
    @Schema(description = "权限项列表", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<PermissionVO> rolePermissions;

    /**
     * 备注。
     */
    @Schema(description = "备注", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String remark;
}