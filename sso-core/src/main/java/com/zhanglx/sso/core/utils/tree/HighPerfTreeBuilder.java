package com.zhanglx.sso.core.utils.tree;

import cn.dev33.satoken.stp.StpUtil;
import com.zhanglx.sso.core.domain.tree.TreeNode;
import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.core.exception.CoreErrorCode;
import com.zhanglx.sso.core.strategy.TreeCycleStrategy;
import com.zhanglx.sso.core.strategy.TreeFilterStrategy;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 作者：Zhang L X
 * 创建时间：2026/3/17 17:07
 * 类名：HighPerfTreeBuilder
 * 说明：高性能树形结构组装器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HighPerfTreeBuilder {

    /**
     * 指标注册器
     */
    private final MeterRegistry meterRegistry;

    @Timed(value = "tree.build.time", description = "Time taken to build the permission tree")
    public <T extends TreeNode<T, Long>> List<T> buildTree(
            List<T> rawData,
            TreeFilterStrategy filterStrategy,
            TreeCycleStrategy cycleStrategy) {
        return buildTree(rawData, filterStrategy, cycleStrategy, null);
    }

    @Timed(value = "tree.build.time", description = "Time taken to build the permission tree")
    public <T extends TreeNode<T, Long>> List<T> buildTree(
            List<T> rawData,
            TreeFilterStrategy filterStrategy,
            TreeCycleStrategy cycleStrategy,
            Comparator<? super T> comparator) {

        if (rawData == null || rawData.isEmpty()) {
            return Collections.emptyList();
        }

        TreeFilterStrategy effectiveFilterStrategy = Objects.requireNonNullElse(filterStrategy, TreeFilterStrategy.NO_FILTER);
        TreeCycleStrategy effectiveCycleStrategy = Objects.requireNonNullElse(cycleStrategy, TreeCycleStrategy.SKIP_CHECK);

        List<T> workingData = prepareWorkingData(rawData, comparator);

        // 非权限树场景不需要取当前登录人的权限集，避免无意义的上下文依赖。
        Set<String> userPermissions = effectiveFilterStrategy == TreeFilterStrategy.NO_FILTER
                ? Collections.emptySet()
                : new HashSet<>(StpUtil.getPermissionList());

        Map<Long, T> nodeMap = workingData.stream()
                .collect(Collectors.toMap(TreeNode::treeNodeId, node -> node, (left, right) -> left, LinkedHashMap::new));

        Set<Long> validIds = effectiveFilterStrategy.calculateValidNodeIds(workingData, nodeMap, userPermissions);
        Set<Long> detachedToRootIds = resolveDetachedToRootIds(workingData, nodeMap, validIds, effectiveCycleStrategy);

        List<T> roots = new ArrayList<>();
        for (T node : workingData) {
            Long nodeId = node.treeNodeId();
            if (!validIds.contains(nodeId)) {
                continue;
            }

            Long parentId = node.treeParentId();
            boolean isRoot = parentId == null || parentId == 0L;
            if (isRoot || detachedToRootIds.contains(nodeId)) {
                roots.add(node);
                continue;
            }

            T parent = nodeMap.get(parentId);
            if (parent != null && validIds.contains(parent.treeNodeId())) {
                parent.treeChildren().add(node);
                continue;
            }

            // 父节点不存在或已被过滤时，降级为根节点，避免整棵树丢失。
            log.warn("Node ID [{}] promoted to root due to missing/unauthorized parent ID [{}]", nodeId, parentId);
            meterRegistry.counter("tree.build.orphan.promoted").increment();
            roots.add(node);
        }

        return roots;
    }

    /**
     * 统一预处理待组树节点，避免多次组树时残留旧的子节点引用。
     * 如果业务需要稳定排序，也统一在这里完成。
     */
    private <T extends TreeNode<T, Long>> List<T> prepareWorkingData(List<T> rawData, Comparator<? super T> comparator) {
        List<T> workingData = new ArrayList<>(rawData);
        if (comparator != null) {
            workingData.sort(comparator);
        }
        for (T node : workingData) {
            node.replaceTreeChildren(new ArrayList<>());
        }
        return workingData;
    }

    /**
     * 检测有效节点之间是否存在环，并根据策略决定是断环继续还是直接失败。
     */
    private <T extends TreeNode<T, Long>> Set<Long> resolveDetachedToRootIds(
            List<T> workingData,
            Map<Long, T> nodeMap,
            Set<Long> validIds,
            TreeCycleStrategy cycleStrategy) {

        if (!cycleStrategy.shouldCheckCycle()) {
            return Collections.emptySet();
        }

        Set<Long> detachedToRootIds = new LinkedHashSet<>();
        Set<Long> resolvedIds = new HashSet<>();

        for (T node : workingData) {
            Long nodeId = node.treeNodeId();
            if (nodeId == null || !validIds.contains(nodeId) || resolvedIds.contains(nodeId)) {
                continue;
            }

            LinkedHashMap<Long, Integer> pathIndex = new LinkedHashMap<>();
            List<Long> path = new ArrayList<>();
            long currentId = nodeId;

            while (currentId != 0L) {
                if (!validIds.contains(currentId) || detachedToRootIds.contains(currentId)) {
                    break;
                }

                Integer existedIndex = pathIndex.putIfAbsent(currentId, path.size());
                if (existedIndex != null) {
                    List<Long> cycleNodeIds = new ArrayList<>(path.subList(existedIndex, path.size()));
                    handleDetectedCycle(cycleNodeIds, nodeMap, cycleStrategy, detachedToRootIds);
                    break;
                }

                path.add(currentId);
                T currentNode = nodeMap.get(currentId);
                if (currentNode == null) {
                    break;
                }

                Long parentId = currentNode.treeParentId();
                if (parentId == null || parentId == 0L || !validIds.contains(parentId) || !nodeMap.containsKey(parentId)) {
                    break;
                }
                currentId = parentId;
            }

            resolvedIds.addAll(path);
        }

        return detachedToRootIds;
    }

    /**
     * 统一处理循环引用。
     * 对查询类场景优先保住树结果，对写操作类场景直接失败，避免脏数据继续扩散。
     */
    private <T extends TreeNode<T, Long>> void handleDetectedCycle(
            List<Long> cycleNodeIds,
            Map<Long, T> nodeMap,
            TreeCycleStrategy cycleStrategy,
            Set<Long> detachedToRootIds) {

        if (cycleNodeIds.isEmpty()) {
            return;
        }

        String cycleDescription = describeCycle(cycleNodeIds, nodeMap);
        meterRegistry.counter("tree.build.cycle.detected").increment();

        if (cycleStrategy.shouldFailFast()) {
            log.error("Detected tree cycle and rejected current build: {}", cycleDescription);
            throw BusinessException.of(CoreErrorCode.TREE_CYCLE_DETECTED, cycleDescription);
        }

        Long detachedNodeId = cycleNodeIds.get(0);
        detachedToRootIds.add(detachedNodeId);
        meterRegistry.counter("tree.build.cycle.broken").increment();
        log.error("Detected tree cycle, detached node [{}] from parent chain: {}", detachedNodeId, cycleDescription);
    }

    /**
     * 生成稳定的循环引用描述，便于日志、告警和国际化错误消息复用。
     */
    private <T extends TreeNode<T, Long>> String describeCycle(List<Long> cycleNodeIds, Map<Long, T> nodeMap) {
        List<Long> loopPath = new ArrayList<>(cycleNodeIds);
        loopPath.add(cycleNodeIds.get(0));
        return loopPath.stream()
                .map(nodeId -> describeNode(nodeMap.get(nodeId), nodeId))
                .collect(Collectors.joining(" -> "));
    }

    private <T extends TreeNode<T, Long>> String describeNode(T node, Long nodeId) {
        if (node == null) {
            return String.valueOf(nodeId);
        }
        String identification = node.treeIdentification();
        if (StringUtils.isNotBlank(identification)) {
            return identification + "(" + nodeId + ")";
        }
        return String.valueOf(nodeId);
    }
}