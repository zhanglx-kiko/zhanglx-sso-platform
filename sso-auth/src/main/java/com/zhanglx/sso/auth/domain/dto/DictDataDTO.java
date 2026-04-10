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
@Schema(name = "DictDataDTO", description = "DictData对象")
public class DictDataDTO extends BaseDTO {

    /**
     * 排序号。
     */
    @Schema(description = "排序号")
    private Integer dictSort;

    /**
     * 字典标签。
     */
    @NotBlank(message = "{dict.label.cannot.be.blank}")
    @Schema(description = "字典标签", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictLabel;

    /**
     * 字典值。
     */
    @NotBlank(message = "{dict.value.cannot.be.blank}")
    @Schema(description = "字典值", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictValue;

    /**
     * 字典类型编码。
     */
    @NotBlank(message = "{dict.type.cannot.be.blank}")
    @Schema(description = "字典类型编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictType;

    /**
     * 状态：1-启用，0-停用。
     */
    @NotNull(message = "{status.cannot.be.blank}")
    @Schema(description = "状态：1-启用，0-停用")
    private EnableStatusEnum status;

    /**
     * 备注。
     */
    @Schema(description = "备注")
    private String remark;
}