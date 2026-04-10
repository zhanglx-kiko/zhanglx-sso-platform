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
 * 字典类型数据传输对象。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DictTypeDTO", description = "字典类型对象")
public class DictTypeDTO extends BaseDTO {

    /**
     * 字典名称。
     */
    @NotBlank(message = "字典名称不能为空")
    @Schema(description = "字典名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictName;

    /**
     * 字典类型编码。
     */
    @NotBlank(message = "字典类型不能为空")
    @Schema(description = "字典类型编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictType;

    /**
     * 状态：1-启用，0-停用。
     */
    @NotNull(message = "状态不能为空")
    @Schema(description = "状态：1-启用，0-停用")
    private EnableStatusEnum status;

    /**
     * 备注。
     */
    @Schema(description = "备注")
    private String remark;
}