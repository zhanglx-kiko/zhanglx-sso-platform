package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.core.domain.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/18 10:13
 * @ClassName: RolePermissionRelationshipMappingDTO
 * @Description:
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "RolePermissionRelationshipMappingDTO", description = "角色与权限项关系映射对象")
public class RolePermissionRelationshipMappingDTO extends BaseDTO {

    /**
     * 角色Id
     */
    @Schema(description = "角色Id", name = "roleId", example = "", type = "Long", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private Long roleId;

    /**
     * 权限项id
     */
    @Schema(description = "权限项id", name = "permissionId", example = "", type = "Long", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private Long permissionId;

    /**
     * 授权过期时间
     */
    @Schema(description = "授权过期时间", name = "expireTime", example = "", type = "LocalDateTime", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private LocalDateTime expireTime;

}
