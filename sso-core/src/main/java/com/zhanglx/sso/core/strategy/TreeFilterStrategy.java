package com.zhanglx.sso.core.strategy;

import com.zhanglx.sso.core.domain.tree.TreeNode;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/17 17:06
 * @ClassName: TreeFilterStrategy
 * @Description: 权限树过滤策略 (V2.0 高性能防弹版)
 */
public enum TreeFilterStrategy {

    /**
     * 策略A：保留有权限的节点及其完整父链（父节点无权限时“穿透”展示，保证菜单结构不被破坏）
     */
    KEEP_PARENT_CHAIN {
        @Override
        public <T extends TreeNode<T, Long>> Set<Long> calculateValidNodeIds(
                Collection<T> allNodes, Map<Long, T> nodeMap, Set<String> userPermissions) {

            // 1. 【极速放行】如果是超级管理员（拥有 * 权限），直接全量放行，拒绝无效计算
            if (isSuperAdmin(userPermissions)) {
                return allNodes.stream().map(TreeNode::getId).collect(Collectors.toSet());
            }

            Set<Long> validIds = new HashSet<>();
            for (T node : allNodes) {
                // 2. 判空保护：排除目录节点可能没有 identification 的情况
                String identification = node.getIdentification();
                if (StringUtils.isNotBlank(identification) && userPermissions.contains(identification)) {

                    // 3. 【性能优化】直接操作 Node 引用向上追溯，省去重复的 nodeMap.get()
                    T currNode = node;
                    while (currNode != null
                            && currNode.getId() != null
                            && currNode.getId() != 0L
                            // Set.add 返回 true 说明是新加入的，返回 false 说明此父链已经被别的子节点打通了，直接 break
                            && validIds.add(currNode.getId())) {

                        currNode = nodeMap.get(currNode.getParentId());
                    }
                }
            }
            return validIds;
        }
    },

    /**
     * 策略B：严格分支隐藏（只要父级没权限，子级哪怕有权限也必须一起被隐藏，防止“孤儿菜单越级挂载”）
     */
    STRICT_BRANCH_HIDE {
        @Override
        public <T extends TreeNode<T, Long>> Set<Long> calculateValidNodeIds(
                Collection<T> allNodes, Map<Long, T> nodeMap, Set<String> userPermissions) {

            if (isSuperAdmin(userPermissions)) {
                return allNodes.stream().map(TreeNode::getId).collect(Collectors.toSet());
            }

            Set<Long> validIds = new HashSet<>();
            for (T node : allNodes) {
                // 判断自身是否有权限
                String identification = node.getIdentification();
                if (StringUtils.isNotBlank(identification) && userPermissions.contains(identification)) {

                    // 【严格校验】必须向上检查所有父节点。只要有一个父节点没权限，当前节点也必须作废！
                    boolean allAncestorsValid = true;
                    T currNode = nodeMap.get(node.getParentId());

                    while (currNode != null && currNode.getId() != null && currNode.getId() != 0L) {
                        String parentIden = currNode.getIdentification();
                        // 允许父级是纯目录（没有标识），如果有标识则必须在权限列表内
                        if (StringUtils.isNotBlank(parentIden) && !userPermissions.contains(parentIden)) {
                            allAncestorsValid = false;
                            break;
                        }
                        currNode = nodeMap.get(currNode.getParentId());
                    }

                    if (allAncestorsValid) {
                        validIds.add(node.getId());
                    }
                }
            }
            return validIds;
        }
    };

    public abstract <T extends TreeNode<T, Long>> Set<Long> calculateValidNodeIds(Collection<T> allNodes, Map<Long, T> nodeMap, Set<String> userPermissions);

    /**
     * 判断是否为超级管理员标识
     */
    protected boolean isSuperAdmin(Set<String> userPermissions) {
        return userPermissions.contains("*") || userPermissions.contains("*:*:*");
    }
}