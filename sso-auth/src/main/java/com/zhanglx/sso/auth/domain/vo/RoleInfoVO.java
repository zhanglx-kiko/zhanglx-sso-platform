package com.zhanglx.sso.auth.domain.vo;

import com.zhanglx.sso.auth.enums.DataScopeEnum;
import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.core.domain.vo.BaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.util.List;

/**
 * 角色详情视图对象。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "RoleInfoVO", description = "角色详情对象")
public class RoleInfoVO extends BaseVO {

    /**
     * 角色名称。
     */
    @Schema(description = "角色名称", accessMode = Schema.AccessMode.READ_ONLY)
    private String roleName;

    /**
     * 角色编码。
     */
    @Schema(description = "角色编码", accessMode = Schema.AccessMode.READ_ONLY)
    private String roleCode;

    /**
     * 应用编码。
     */
    @Schema(description = "应用编码", accessMode = Schema.AccessMode.READ_ONLY)
    private String appCode;

    /**
     * 数据范围。
     */
    @Schema(description = "数据范围", accessMode = Schema.AccessMode.READ_ONLY)
    private DataScopeEnum dataScope;

    /**
     * 状态。
     */
    @Schema(description = "状态", accessMode = Schema.AccessMode.READ_ONLY)
    private EnableStatusEnum status;

    /**
     * 已绑定的用户 ID 列表。
     */
    @Schema(description = "已绑定的用户 ID 列表", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonSerialize(contentUsing = ToStringSerializer.class)
    private List<Long> userIds;
}