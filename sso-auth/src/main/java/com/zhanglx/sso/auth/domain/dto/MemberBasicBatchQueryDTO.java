package com.zhanglx.sso.auth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(name = "MemberBasicBatchQueryDTO", description = "会员基础信息批量查询参数")
public class MemberBasicBatchQueryDTO {

    @NotEmpty(message = "parameter.error")
    @Size(max = 200, message = "parameter.error")
    @Schema(description = "会员 ID 列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<@NotNull(message = "parameter.error") Long> memberIds;
}
