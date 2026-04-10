package com.zhanglx.sso.auth.service.tree;

import com.zhanglx.sso.auth.domain.dto.DeptDTO;
import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.core.exception.CoreErrorCode;
import com.zhanglx.sso.core.strategy.TreeCycleStrategy;
import com.zhanglx.sso.core.strategy.TreeFilterStrategy;
import com.zhanglx.sso.core.utils.tree.HighPerfTreeBuilder;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HighPerfTreeBuilderTest {

    private final HighPerfTreeBuilder treeBuilder = new HighPerfTreeBuilder(new SimpleMeterRegistry());

    @Test
    void shouldBuildDeptTreeWithComparatorAndClearStaleChildren() {
        DeptDTO staleChild = dept(99L, 1L, 99, "stale-child");
        DeptDTO rootA = dept(1L, 0L, 1, "root-a");
        rootA.setChildren(new ArrayList<>(List.of(staleChild)));
        DeptDTO child = dept(3L, 1L, 2, "child");
        DeptDTO rootB = dept(2L, 0L, 3, "root-b");

        Comparator<DeptDTO> comparator = Comparator.comparing(DeptDTO::getSortNum, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(DeptDTO::getId, Comparator.nullsLast(Long::compareTo));

        List<DeptDTO> roots = treeBuilder.buildTree(
                List.of(rootB, child, rootA),
                TreeFilterStrategy.NO_FILTER,
                TreeCycleStrategy.SKIP_CHECK,
                comparator);

        assertEquals(2, roots.size());
        assertEquals(1L, roots.get(0).getId());
        assertEquals(2L, roots.get(1).getId());

        assertNotNull(rootA.getChildren());
        assertEquals(1, rootA.getChildren().size());
        assertEquals(3L, rootA.getChildren().get(0).getId());
        assertTrue(rootA.getChildren().stream().noneMatch(item -> item.getId().equals(99L)));
    }

    @Test
    void shouldBreakCycleAndPromoteDetachedNodeToRoot() {
        DeptDTO node1 = dept(1L, 2L, 1, "node-1");
        DeptDTO node2 = dept(2L, 3L, 2, "node-2");
        DeptDTO node3 = dept(3L, 1L, 3, "node-3");
        DeptDTO node4 = dept(4L, 0L, 4, "node-4");

        List<DeptDTO> roots = treeBuilder.buildTree(
                List.of(node1, node2, node3, node4),
                TreeFilterStrategy.NO_FILTER,
                TreeCycleStrategy.BREAK_AND_CONTINUE,
                Comparator.comparing(DeptDTO::getId));

        assertEquals(2, roots.size());
        assertEquals(1L, roots.get(0).getId());
        assertEquals(4L, roots.get(1).getId());
        assertEquals(1, node1.getChildren().size());
        assertEquals(3L, node1.getChildren().get(0).getId());
        assertEquals(1, node3.getChildren().size());
        assertEquals(2L, node3.getChildren().get(0).getId());
    }

    @Test
    void shouldFailFastWhenCycleStrategyIsFailFast() {
        DeptDTO node1 = dept(1L, 2L, 1, "node-1");
        DeptDTO node2 = dept(2L, 1L, 2, "node-2");

        BusinessException exception = assertThrows(BusinessException.class, () -> treeBuilder.buildTree(
                List.of(node1, node2),
                TreeFilterStrategy.NO_FILTER,
                TreeCycleStrategy.FAIL_FAST,
                Comparator.comparing(DeptDTO::getId)));

        assertEquals(CoreErrorCode.TREE_CYCLE_DETECTED.getCode(), exception.getCode());
        assertEquals(CoreErrorCode.TREE_CYCLE_DETECTED.getMessageKey(), exception.getMessageKey());
        assertInstanceOf(String.class, exception.getArgs()[0]);
        assertTrue(String.valueOf(exception.getArgs()[0]).contains("1"));
        assertTrue(String.valueOf(exception.getArgs()[0]).contains("2"));
    }

    private DeptDTO dept(Long id, Long parentId, Integer sortNum, String deptName) {
        DeptDTO deptDTO = new DeptDTO();
        deptDTO.setId(id);
        deptDTO.setParentId(parentId);
        deptDTO.setSortNum(sortNum);
        deptDTO.setDeptName(deptName);
        return deptDTO;
    }
}
