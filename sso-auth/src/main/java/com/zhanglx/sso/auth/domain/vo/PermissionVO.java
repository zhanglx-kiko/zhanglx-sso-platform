package com.zhanglx.sso.auth.domain.vo;

import com.zhanglx.sso.core.domain.vo.BaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/17 14:44
 * @ClassName: PermissionVO
 * @Description:
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "PermissionVO", description = "权限对象")
public class PermissionVO extends BaseVO {

    /**
     * 权限项名称
     */
    @Schema(description = "权限项名称", name = "name", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    @NotBlank(message = "权限项名称不能为空")
    private String name;

    /**
     * 权限项标识
     */
    @Schema(description = "权限项标识", name = "identification", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    @NotBlank(message = "权限项标识不能为空")
    private String identification;

    /**
     * 父ID
     */
    @Schema(description = "父ID", name = "parentId", example = "", type = "Long", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private Long parentId;

    /**
     * 标识血缘
     */
    @Schema(description = "标识血缘", name = "identityLineage", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private String identityLineage;

    /**
     * 组件地址
     */
    @Schema(description = "组件地址", name = "comPath", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private String comPath;

    /**
     * 路由地址
     */
    @Schema(description = "路由地址", name = "path", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private String path;

    /**
     * 菜单图标
     */
    @Schema(description = "菜单图标", name = "iconStr", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private String iconStr;

    /**
     * 显示顺序
     */
    @Schema(description = "显示顺序", name = "displayNo", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private Integer displayNo;

    /**
     * 是否为外链(0是 1否)
     */
    @Schema(description = "是否为外链(0是 1否)", name = "isFrame", example = "", type = "Short", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private Short isFrame;

    /**
     * 类型(-1平台 0模块 1菜单 2按钮 3接口)
     */
    @Schema(description = "类型(-1平台 0模块 1菜单 2按钮 3 接口)", name = "type", example = "", type = "Short", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    @NotNull(message = "类型不能为空")
    private Short type;

    /**
     * 备注
     */
    @Schema(description = "备注", name = "remark", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private String remark;

}
