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
    @NotBlank(message = "{config.name.cannot.be.blank}")
    @Schema(description = "参数名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configName;

    /**
     * 参数键。
     */
    @NotBlank(message = "{config.key.cannot.be.blank}")
    @Schema(description = "参数键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configKey;

    /**
     * 参数值。
     */
    @NotBlank(message = "{config.value.cannot.be.blank}")
    @Schema(description = "参数值", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configValue;

    /**
     * 参数分组。
     */
    @NotBlank(message = "{config.group.cannot.be.blank}")
    @Schema(description = "参数分组", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configGroup;

    /**
     * 是否敏感参数。
     */
    @NotNull(message = "{config.sensitive.flag.cannot.be.blank}")
    @Schema(description = "是否敏感参数")
    private YesNoEnum sensitiveFlag;

    /**
     * 参数状态。
     */
    @NotNull(message = "{config.status.cannot.be.blank}")
    @Schema(description = "参数状态")
    private EnableStatusEnum status;

    /**
     * 参数类型。
     */
    @NotNull(message = "{config.type.cannot.be.blank}")
    @Schema(description = "参数类型")
    private ConfigTypeEnum configType;

    /**
     * 备注。
     */
    @Schema(description = "备注")
    private String remark;
}
