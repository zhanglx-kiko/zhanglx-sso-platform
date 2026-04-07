package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.core.domain.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DictDataDTO", description = "字典数据对象")
public class DictDataDTO extends BaseDTO {

    @Schema(description = "排序号")
    private Integer dictSort;

    @NotBlank(message = "字典标签不能为空")
    @Schema(description = "字典标签", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictLabel;

    @NotBlank(message = "字典值不能为空")
    @Schema(description = "字典值", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictValue;

    @NotBlank(message = "字典类型不能为空")
    @Schema(description = "字典类型编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictType;

    @Min(value = 0, message = "状态只能为 0 或 1")
    @Max(value = 1, message = "状态只能为 0 或 1")
    @Schema(description = "状态：1-启用，0-停用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
