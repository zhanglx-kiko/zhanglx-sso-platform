package com.zhanglx.sso.core.domain.tree;

import java.util.List;

/**
 * @param <T>  节点类型
 * @param <ID> 节点标识类型
 *             作者：Zhang L X
 *             创建时间：2026/3/17 17:04
 *             类名：TreeNode
 *             说明：通用树节点能力接口
 *
 * 这里不再使用 getId/setId/getParentId 这类 Bean 风格命名，
 * 避免和 DTO/PO 基类中的属性访问器发生桥接方法冲突。
 */
public interface TreeNode<T, ID> {

    /**
     * 获取当前节点标识
     */
    ID treeNodeId();

    /**
     * 获取父节点标识
     */
    ID treeParentId();

    /**
     * 在树导入、拍平等场景下重设父节点标识
     */
    void assignTreeParentId(ID parentId);

    /**
     * 获取子节点集合
     */
    List<T> treeChildren();

    /**
     * 替换子节点集合
     */
    void replaceTreeChildren(List<T> children);

    /**
     * 获取权限标识，非权限树可返回 null
     */
    default String treeIdentification() {
        return null;
    }
}
