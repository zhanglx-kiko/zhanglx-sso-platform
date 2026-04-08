package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.core.config.StringToLongDeserializer;
import com.zhanglx.sso.core.domain.dto.BaseDTO;
import com.zhanglx.sso.core.domain.tree.TreeNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DeptDTO", description = "部门对象")
public class DeptDTO extends BaseDTO implements TreeNode<DeptDTO, Long> {

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(description = "父部门ID")
    private Long parentId;

    @Schema(description = "祖级列表")
    private String ancestors;

    @NotBlank(message = "部门名称不能为空")
    @Schema(description = "部门名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String deptName;

    @Schema(description = "排序号")
    private Integer sortNum;

    @Schema(description = "启停状态")
    private EnableStatusEnum status;

    @Builder.Default
    @Schema(description = "子部门")
    private List<DeptDTO> children = new ArrayList<>();

    @Override
    public String getIdentification() {
        return null;
    }
}