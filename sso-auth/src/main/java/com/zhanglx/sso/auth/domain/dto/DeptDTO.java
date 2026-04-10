package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.core.domain.dto.BaseTreeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 部门数据传输对象。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DeptDTO", description = "部门对象")
public class DeptDTO extends BaseTreeDTO<DeptDTO> {

    /**
     * 祖级列表。
     */
    @Schema(description = "祖级列表")
    private String ancestors;

    /**
     * 部门名称。
     */
    @NotBlank(message = "{dept.name.cannot.be.blank}")
    @Schema(description = "部门名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String deptName;

    /**
     * 排序号。
     */
    @Schema(description = "排序号")
    private Integer sortNum;

    /**
     * 启停状态。
     */
    @Schema(description = "启停状态")
    private EnableStatusEnum status;
}
