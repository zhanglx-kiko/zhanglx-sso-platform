package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.auth.enums.ConfigTypeEnum;
import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.auth.enums.YesNoEnum;
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
 * 系统配置数据传输对象。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "ConfigDTO", description = "系统参数对象")
public class ConfigDTO extends BaseDTO {

    /**
     * 参数名称。
     */
    @NotBlank(message = "参数名称不能为空")
    @Schema(description = "参数名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configName;

    /**
     * 参数键。
     */
    @NotBlank(message = "参数键不能为空")
    @Schema(description = "参数键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configKey;

    /**
     * 参数值。
     */
    @NotBlank(message = "参数值不能为空")
    @Schema(description = "参数值", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configValue;

    /**
     * 参数分组。
     */
    @NotBlank(message = "参数分组不能为空")
    @Schema(description = "参数分组", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configGroup;

    /**
     * 是否敏感参数。
     */
    @NotNull(message = "敏感标识不能为空")
    @Schema(description = "是否敏感参数")
    private YesNoEnum sensitiveFlag;

    /**
     * 参数状态。
     */
    @NotNull(message = "参数状态不能为空")
    @Schema(description = "参数状态")
    private EnableStatusEnum status;

    /**
     * 参数类型。
     */
    @NotNull(message = "参数类型不能为空")
    @Schema(description = "参数类型")
    private ConfigTypeEnum configType;

    /**
     * 备注。
     */
    @Schema(description = "备注")
    private String remark;
}
