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
@Schema(name = "AppDTO", description = "接入应用对象")
public class AppDTO extends BaseDTO {

    @NotBlank(message = "应用编码不能为空")
    @Schema(description = "应用编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appCode;

    @NotBlank(message = "应用名称不能为空")
    @Schema(description = "应用名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appName;

    @Min(value = 0, message = "状态只能为 0 或 1")
    @Max(value = 1, message = "状态只能为 0 或 1")
    @Schema(description = "状态：1-启用，0-停用")
    private Integer status;

    @Min(value = 1, message = "用户类型只能为 1 或 2")
    @Max(value = 2, message = "用户类型只能为 1 或 2")
    @Schema(description = "用户类型：1-系统用户，2-会员用户")
    private Integer userType;

    @Schema(description = "备注")
    private String remark;
}
