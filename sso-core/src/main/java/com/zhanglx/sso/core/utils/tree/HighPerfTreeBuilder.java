package com.zhanglx.sso.core.utils.tree;

import cn.dev33.satoken.stp.StpUtil;
import com.zhanglx.sso.core.domain.tree.TreeNode;
import com.zhanglx.sso.core.strategy.TreeFilterStrategy;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 作者：Zhang L X
 * 创建时间：2026/3/17 17:07
 * 类名：HighPerf树构建器
 * 说明：高性能树形结构组装器（零时域分配优化版）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HighPerfTreeBuilder {
    /**
     * 指标注册器。
     */
    private final MeterRegistry meterRegistry;

    @Timed(value = "tree.build.time", description = "Time taken to build the permission tree")
    public <T extends TreeNode<T, Long>> List<T> buildTree(
            List<T> rawData,
            TreeFilterStrategy strategy,
            boolean checkCycle) {

        if (rawData == null || rawData.isEmpty()) return Collections.emptyList();

        // 1. 获取 Sa-Token 当前用户权限 (放入 Set 加速 O(1) 查询)
        Set<String> userPermissions = strategy == TreeFilterStrategy.NO_FILTER
                ? Collections.emptySet()
                : new HashSet<>(StpUtil.getPermissionList());

        // 2. 建立索引 (利用 JDK 增强的 SequencedMap 保持顺序可预见性)
        Map<Long, T> nodeMap = rawData.stream()
                .collect(Collectors.toMap(TreeNode::getId, node -> node, (k1, k2) -> k1, LinkedHashMap::new));

        // 3. 计算有效节点 ID 集合 (构建时过滤核心)
        Set<Long> validIds = strategy.calculateValidNodeIds(rawData, nodeMap, userPermissions);

        // 4. O(N) 一次遍历组装树 + 容错处理
        List<T> roots = new ArrayList<>();
        for (T node : rawData) {
            if (!validIds.contains(node.getId())) {
                continue; // 实时剔除无权限节点
            }

            Long parentId = node.getParentId();
            // 优化：利用 Java 自动拆箱特性，前提是有 != null 护航，性能最高且避免 Long 缓存陷阱
            boolean isRoot = (parentId == null || parentId == 0L);

            if (isRoot) {
                roots.add(node);
            } else {
                T parent = nodeMap.get(parentId);
                if (parent != null && validIds.contains(parent.getId())) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(node);
                } else {
                    // 容错降级：父节点缺失或无权限，自动将当前节点提升为临时根节点
                    log.warn("Node ID [{}] promoted to root due to missing/unauthorized parent ID [{}]", node.getId(), parentId);
                    meterRegistry.counter("tree.build.orphan.promoted").increment();
                    roots.add(node);
                }
            }
        }

        // 5. 循环依赖检测与打破
        if (checkCycle) {
            detectAndBreakCycles(roots);
        }

        return roots;
    }

    /**
     * 【优化】循环引用检测：使用回溯 DFS，全程仅分配 1 个 HashSet
     */
    private <T extends TreeNode<T, Long>> void detectAndBreakCycles(List<T> roots) {
        // 全局只 new 一次 HashSet，充当递归路径的检测栈
        Set<Long> onStack = new HashSet<>();
        for (T root : roots) {
            dfsCheckCycle(root, onStack);
        }
    }

    private <T extends TreeNode<T, Long>> void dfsCheckCycle(T node, Set<Long> onStack) {
        // 当前节点入栈，标记为正在访问
        onStack.add(node.getId());

        List<T> children = node.getChildren();
        if (children != null) {
            Iterator<T> iterator = children.iterator();
            while (iterator.hasNext()) {
                T child = iterator.next();

                // 如果子节点已经在当前的访问栈中，说明形成了闭环！
                if (onStack.contains(child.getId())) {
                    log.error("Breaking loop: Node {} -> Node {}", node.getId(), child.getId());
                    iterator.remove(); // 斩断循环引用
                    meterRegistry.counter("tree.build.cycle.detected").increment();
                } else {
                    // 继续向下深搜
                    dfsCheckCycle(child, onStack);
                }
            }
        }

        // 【灵魂代码】：回溯！当前节点及其子孙全部探测完毕，出栈
        onStack.remove(node.getId());
    }

}