package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.auth.enums.ConfigTypeEnum;
import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.auth.enums.YesNoEnum;
import com.zhanglx.sso.core.domain.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 系统配置分页查询参数对象。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "ConfigQueryDTO", description = "系统参数分页查询参数")
public class ConfigQueryDTO extends PageQuery {

    /**
     * 参数名称。
     */
    @Schema(description = "参数名称")
    private String configName;

    /**
     * 参数键。
     */
    @Schema(description = "参数键")
    private String configKey;

    /**
     * 参数分组。
     */
    @Schema(description = "参数分组")
    private String configGroup;

    /**
     * 是否敏感参数。
     */
    @Schema(description = "是否敏感参数")
    private YesNoEnum sensitiveFlag;

    /**
     * 参数状态。
     */
    @Schema(description = "参数状态")
    private EnableStatusEnum status;

    /**
     * 是否系统内置：1-是，0-否。
     */
    @Schema(description = "是否系统内置：1-是，0-否")
    private ConfigTypeEnum configType;
}
