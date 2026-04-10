package com.zhanglx.sso.auth.domain.vo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.hibernate.validator.constraints.Length;

/**
 * 作者：Zhang L X
 * 说明：权限导入、导出 Excel 接收对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PermissionExcelVO {

    /**
     * 类型。
     */
    @ExcelProperty(value = "类型(-1平台 0模块 1菜单 2按钮 3接口)", index = 0)
    @NotNull(message = "类型不能为空")
    @Min(value = -1, message = "类型暂只支持-1到3")
    @Max(value = 3, message = "类型暂只支持-1到3")
    private Integer type;

    /**
     * 父级标识。
     */
    @ExcelProperty(value = "上级identification", index = 1)
    private String parentIdentification;

    /**
     * 名称。
     */
    @ExcelProperty(value = "权限项名称", index = 2)
    @NotBlank(message = "权限项名称不能为空")
    @Length(max = 16, message = "权限项名称不能超过16个字符")
    private String name;

    /**
     * 标识。
     */
    @ExcelProperty(value = "权限项标识", index = 3)
    @NotBlank(message = "权限项标识不能为空")
    @Length(max = 128, message = "权限项标识不能超过128个字符")
    private String identification;

    /**
     * 路径。
     */
    @ExcelProperty(value = "路由地址", index = 4)
    @Length(max = 256, message = "路由地址不能超过256个字符")
    private String path;

    /**
     * 组件路径。
     */
    @ExcelProperty(value = "组件地址", index = 5)
    @Length(max = 256, message = "组件地址不能超过256个字符")
    private String comPath;

    /**
     * 是否为外链。
     */
    @ExcelProperty(value = "是否为外链(0是 1否)", index = 6)
    @Min(value = 0, message = "是否外链只能输入0或1")
    @Max(value = 1, message = "是否外链只能输入0或1")
    private Integer isFrame;

    /**
     * 图标。
     */
    @ExcelProperty(value = "菜单图标", index = 7)
    @Length(max = 64, message = "菜单图标不能超过64个字符")
    private String iconStr;

    /**
     * 显示顺序。
     */
    @ExcelProperty(value = "显示序号", index = 8)
    @NotNull(message = "显示序号不能为空")
    @Min(value = 1, message = "显示序号不能小于1")
    @Max(value = 999, message = "显示序号不能大于999")
    private Integer displayNo;

    /**
     * 备注。
     */
    @ExcelProperty(value = "备注", index = 9)
    @Length(max = 256, message = "备注不能超过256个字符")
    private String remark;

    /**
     * 错误信息。
     */
    @ExcelProperty(value = "导入失败原因", index = 10)
    private String errorMessage;
}