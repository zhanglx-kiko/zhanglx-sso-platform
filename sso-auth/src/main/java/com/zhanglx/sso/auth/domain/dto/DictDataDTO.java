package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.core.domain.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
/**
 * 字典数据数据传输对象。
 */


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

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态：1-启用，0-停用")
    private EnableStatusEnum status;

    @Schema(description = "备注")
    private String remark;
}
