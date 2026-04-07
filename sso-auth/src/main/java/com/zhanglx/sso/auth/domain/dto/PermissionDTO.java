package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.core.config.StringToLongDeserializer;
import com.zhanglx.sso.core.domain.dto.BaseDTO;
import com.zhanglx.sso.core.domain.tree.TreeNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限项对象。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "PermissionDTO", description = "权限项对象")
public class PermissionDTO extends BaseDTO implements TreeNode<PermissionDTO, Long> {

    /**
     * 权限项名称。
     */
    @NotBlank(message = "权限项名称不能为空")
    @Schema(description = "权限项名称", name = "name", example = "平台 模块 菜单 按钮 接口", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private String name;

    /**
     * 权限项标识。
     */
    @NotBlank(message = "权限项标识不能为空")
    @Schema(description = "权限项标识", name = "identification", example = "system:user:list", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private String identification;

    /**
     * 父级 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(description = "父级ID", name = "parentId", example = "0", type = "Long", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private Long parentId;

    /**
     * 标识血缘。
     */
    @Schema(description = "标识血缘", name = "identityLineage", example = "platform/system/user", type = "String", requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private String identityLineage;

    /**
     * 组件地址。
     */
    @Schema(description = "组件地址", name = "comPath", example = "/system/user/index", type = "String", requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private String comPath;

    /**
     * 路由地址。
     */
    @Schema(description = "路由地址", name = "path", example = "/system/user", type = "String", requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private String path;

    /**
     * 菜单图标。
     */
    @Schema(description = "菜单图标", name = "iconStr", example = "user", type = "String", requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private String iconStr;

    /**
     * 显示顺序。
     */
    @Schema(description = "显示顺序", name = "displayNo", example = "1", type = "Integer", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private Integer displayNo;

    /**
     * 是否为外链，1 是，0 否。
     */
    @Builder.Default
    @Schema(description = "是否为外链，1 是，0 否", name = "isFrame", example = "0", type = "Integer", defaultValue = "0", allowableValues = {"1", "0"}, requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private Integer isFrame = 0;

    /**
     * 类型，-1 平台，0 模块，1 菜单，2 按钮，3 接口。
     */
    @NotNull(message = "类型不能为空")
    @Schema(description = "类型，-1 平台，0 模块，1 菜单，2 按钮，3 接口", name = "type", allowableValues = {"-1", "0", "1", "2", "3"}, type = "Integer", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private Integer type;

    /**
     * 备注。
     */
    @Schema(description = "备注", name = "remark", example = "系统用户菜单", type = "String", requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private String remark;

    /**
     * 子权限项。
     */
    @Builder.Default
    @Schema(description = "子权限项", name = "children", type = "List<PermissionDTO>", requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private List<PermissionDTO> children = new ArrayList<>();

    @Schema(description = "状态", name = "status", example = "1", type = "Integer", requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_WRITE)
    private Integer status;
}
