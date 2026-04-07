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
@Schema(name = "ConfigDTO", description = "系统参数对象")
public class ConfigDTO extends BaseDTO {

    @NotBlank(message = "参数名称不能为空")
    @Schema(description = "参数名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configName;

    @NotBlank(message = "参数键不能为空")
    @Schema(description = "参数键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configKey;

    @NotBlank(message = "参数值不能为空")
    @Schema(description = "参数值", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configValue;

    @Min(value = 0, message = "内置标记只能为 0 或 1")
    @Max(value = 1, message = "内置标记只能为 0 或 1")
    @Schema(description = "是否系统内置：1-是，0-否")
    private Integer configType;

    @Schema(description = "备注")
    private String remark;
}
