package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.core.domain.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
/**
 * 岗位数据传输对象。
 */


@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "PostDTO", description = "岗位对象")
public class PostDTO extends BaseDTO {

    @NotBlank(message = "岗位编码不能为空")
    @Schema(description = "岗位编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String postCode;

    @NotBlank(message = "岗位名称不能为空")
    @Schema(description = "岗位名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String postName;

    @Schema(description = "排序号")
    private Integer sortNum;

    @Schema(description = "启停状态")
    private EnableStatusEnum status;
}
