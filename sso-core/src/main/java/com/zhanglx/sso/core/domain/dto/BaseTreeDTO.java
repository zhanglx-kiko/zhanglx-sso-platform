package com.zhanglx.sso.core.domain.dto;

import com.zhanglx.sso.core.config.StringToLongDeserializer;
import com.zhanglx.sso.core.domain.tree.TreeNode;
import io.swagger.v3.oas.annotations.media.Schema;
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

/**
 * 作者：Zhang L X
 * 创建时间：2026/4/11 03:00
 * 类名：BaseTreeDTO
 * 说明：树形 DTO 基类，统一沉淀父子结构字段与树能力实现
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "BaseTreeDTO", description = "树形结构基础数据传输对象")
public abstract class BaseTreeDTO<T> extends BaseDTO implements TreeNode<T, Long> {

    /**
     * 父节点标识
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(description = "父节点ID", type = "String", accessMode = Schema.AccessMode.READ_WRITE)
    private Long parentId;

    /**
     * 子节点集合
     */
    @Builder.Default
    @Schema(description = "子节点集合", accessMode = Schema.AccessMode.READ_ONLY)
    private List<T> children = new ArrayList<>();

    @Override
    public final Long treeNodeId() {
        return getId();
    }

    @Override
    public final Long treeParentId() {
        return parentId;
    }

    @Override
    public final void assignTreeParentId(Long parentId) {
        this.parentId = parentId;
    }

    @Override
    public final List<T> treeChildren() {
        return children;
    }

    @Override
    public final void replaceTreeChildren(List<T> children) {
        this.children = children;
    }
}
