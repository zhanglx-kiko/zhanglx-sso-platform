package com.zhanglx.sso.auth.domain.vo;

import com.zhanglx.sso.core.domain.vo.BaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/18 09:52
 * @ClassName: RoleInfoVO
 * @Description:
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "RoleInfoVO", description = "角色授权页面对象")
public class RoleInfoVO extends BaseVO {

    /**
     * 角色名称
     */
    @Schema(description = "角色名称", name = "roleName", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private String roleName;

    /**
     * 角色编码
     */
    @Schema(description = "角色编码", name = "roleCode", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private String roleCode;

    /**
     * 角色类型
     */
    @Schema(description = "角色类型", name = "roleType", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private String roleType;

    /**
     * 已经绑定的用户id组
     */
    @Schema(description = "已经绑定的用户id", name = "userIds", example = "", type = "List<Long>", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private List<Long> userIds;

}
