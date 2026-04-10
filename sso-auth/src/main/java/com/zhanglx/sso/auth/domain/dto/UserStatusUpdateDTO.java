package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.auth.enums.UserStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户状态更新请求对象。
 */
@Data
@Schema(name = "UserStatusUpdateDTO", description = "用户状态更新参数")
public class UserStatusUpdateDTO {

    /**
     * 用户状态。
     */
    @NotNull(message = "状态不能为空")
    @Schema(description = "用户状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private UserStatusEnum status;
}