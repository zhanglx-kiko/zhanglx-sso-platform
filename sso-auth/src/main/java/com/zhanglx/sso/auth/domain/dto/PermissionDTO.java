package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.core.domain.dto.BaseDTO;
import com.zhanglx.sso.core.domain.tree.TreeNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/17 09:48
 * @ClassName: PermissionDTO
 * @Description: 权限项对象
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "PermissionDTO", description = "权限项对象")
public class PermissionDTO extends BaseDTO implements TreeNode<PermissionDTO, Long> {

    /**
     * 权限项名称
     */
    @NotBlank(message = "权限项名称不能为空")
    @Schema(description = "权限项名称", name = "name", example = "平台 模块 菜单 按钮 接口", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private String name;

    /**
     * 权限项标识
     */
    @NotBlank(message = "权限项标识不能为空")
    @Schema(description = "权限项标识", name = "identification", example = "权限项标识", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private String identification;

    /**
     * 父ID
     */
    @Schema(description = "父ID", name = "parentId", example = "父ID", type = "Long", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private Long parentId;

    /**
     * 标识血缘
     */
    @Schema(description = "标识血缘", name = "identityLineage", example = "标识血缘", type = "String", requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private String identityLineage;

    /**
     * 组件地址
     */
    @Schema(description = "组件地址", name = "comPath", example = "组件地址", type = "String", requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private String comPath;

    /**
     * 路由地址
     */
    @Schema(description = "路由地址", name = "path", example = "路由地址", type = "String", requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private String path;

    /**
     * 菜单图标
     */
    @Schema(description = "菜单图标", name = "iconStr", example = "菜单图标", type = "String", requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private String iconStr;

    /**
     * 显示顺序
     */
    @Schema(description = "显示顺序", name = "displayNo", example = "显示顺序", type = "Integer", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private Integer displayNo;

    /**
     * 是否为外链(1是 0否)
     */
    @Builder.Default
    @Schema(description = "是否为外链(1是 0否)", name = "isFrame", example = "是否为外链", type = "Short", defaultValue = "0", allowableValues = {"1", "0"}, requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private Short isFrame = 0;

    /**
     * 类型(-1平台 0模块 1菜单 2按钮 3接口)
     */
    @NotNull(message = "类型不能为空")
    @Schema(description = "类型(-1平台 0模块 1菜单 2按钮 3 接口)", name = "type", example = "", allowableValues = {"-1", "0", "1", "2", "3"}, type = "Short", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private Short type;

    /**
     * 备注
     */
    @Schema(description = "备注", name = "remark", example = "备注", type = "String", requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private String remark;

    /**
     * 子权限项
     */
    @Builder.Default
    @Schema(description = "子权限项", name = "children", example = "", type = "List<PermissionDTO>", requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private List<PermissionDTO> children = new ArrayList<PermissionDTO>();

}

