package com.zhanglx.sso.core.domain.tree;

import java.util.List;

/**
 * @param <T>  实体类型（如 PermissionDTO）
 * @param <ID> 主键类型（如 Long, String）
 * @Author: Zhang L X
 * @Create: 2026/3/17 17:04
 * @ClassName: TreeNode
 * @Description: 通用树节点接口（泛型化）
 */
public interface TreeNode<T, ID> {

    ID getId();

    void setId(ID id);

    ID getParentId();

    // 通用拍平算法需要动态修改父ID
    void setParentId(ID parentId);

    List<T> getChildren();

    // 通用拍平算法需要清空子节点引用
    void setChildren(List<T> children);

    // 权限标识（若非权限树，可由实现类返回 null 或默认值）
    String getIdentification();
}
