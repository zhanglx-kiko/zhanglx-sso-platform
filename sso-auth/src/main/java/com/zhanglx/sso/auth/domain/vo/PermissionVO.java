package com.zhanglx.sso.auth.domain.vo;

import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.auth.enums.PermissionTypeEnum;
import com.zhanglx.sso.auth.enums.YesNoEnum;
import com.zhanglx.sso.core.config.StringToLongDeserializer;
import com.zhanglx.sso.core.domain.vo.BaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

/**
 * 权限对象。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "PermissionVO", description = "权限对象")
public class PermissionVO extends BaseVO {

    /**
     * 权限项名称。
     */
    @Schema(description = "权限项名称", name = "name", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    @NotBlank(message = "权限项名称不能为空")
    private String name;

    /**
     * 权限项标识。
     */
    @Schema(description = "权限项标识", name = "identification", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    @NotBlank(message = "权限项标识不能为空")
    private String identification;

    /**
     * 父级 标识。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(description = "父级ID", name = "parentId", example = "", type = "Long", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private Long parentId;

    /**
     * 标识血缘。
     */
    @Schema(description = "标识血缘", name = "identityLineage", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private String identityLineage;

    /**
     * 组件地址。
     */
    @Schema(description = "组件地址", name = "comPath", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private String comPath;

    /**
     * 路由地址。
     */
    @Schema(description = "路由地址", name = "path", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private String path;

    /**
     * 菜单图标。
     */
    @Schema(description = "菜单图标", name = "iconStr", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private String iconStr;

    /**
     * 显示顺序。
     */
    @Schema(description = "显示顺序", name = "displayNo", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private Integer displayNo;

    /**
     * 是否为外链，0 否，1 是。
     */
    @Schema(description = "是否为外链，0 否，1 是", name = "isFrame", example = "", type = "Integer", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private YesNoEnum isFrame;

    /**
     * 类型，-1 平台，0 模块，1 菜单，2 按钮，3 接口。
     */
    @Schema(description = "类型，-1 平台，0 模块，1 菜单，2 按钮，3 接口", name = "type", example = "", type = "Integer", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    @NotNull(message = "类型不能为空")
    private PermissionTypeEnum type;

    /**
     * 备注。
     */
    @Schema(description = "备注", name = "remark", example = "", type = "String", requiredMode = Schema.RequiredMode.REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private String remark;

    /**
     * 状态。
     */
    @Schema(description = "状态", name = "status", example = "", type = "Integer", requiredMode = Schema.RequiredMode.NOT_REQUIRED, accessMode = Schema.AccessMode.READ_ONLY)
    private EnableStatusEnum status;
}