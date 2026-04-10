package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 启停状态更新请求对象。
 */
@Data
@Schema(name = "EnableStatusUpdateDTO", description = "启停状态更新参数")
public class EnableStatusUpdateDTO {

    /**
     * 启停状态。
     */
    @NotNull(message = "{status.cannot.be.blank}")
    @Schema(description = "启停状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private EnableStatusEnum status;
}