package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.auth.enums.UserTypeEnum;
import com.zhanglx.sso.core.domain.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 应用数据传输对象。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "AppDTO", description = "接入应用对象")
public class AppDTO extends BaseDTO {

    /**
     * 应用编码。
     */
    @NotBlank(message = "应用编码不能为空")
    @Schema(description = "应用编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appCode;

    /**
     * 应用名称。
     */
    @NotBlank(message = "应用名称不能为空")
    @Schema(description = "应用名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appName;

    /**
     * 启停状态。
     */
    @Schema(description = "启停状态")
    private EnableStatusEnum status;

    /**
     * 用户类型。
     */
    @Schema(description = "用户类型")
    private UserTypeEnum userType;

    /**
     * 备注。
     */
    @Schema(description = "备注")
    private String remark;
}
