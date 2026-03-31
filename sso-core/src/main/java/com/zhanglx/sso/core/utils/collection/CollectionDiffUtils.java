package com.zhanglx.sso.core.utils.collection;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/19 11:27
 * @ClassName: CollectionDiffUtils
 * @Description: 通用集合差异比对工具
 */
public class CollectionDiffUtils {

    // 使用 Record 定义数据载体，天生不可变且自带 equals/hashCode
    public record DiffResult<T>(Set<T> toAdd, Set<T> toDelete) {
        public boolean hasChanges() {
            return !toAdd.isEmpty() || !toDelete.isEmpty();
        }
    }

    public static <T> DiffResult<T> compare(Collection<T> existingItems, Collection<T> newItems) {
        Set<T> existingSet = existingItems == null ? Collections.emptySet() :
                (existingItems instanceof Set<T> s ? s : new HashSet<>(existingItems));

        Set<T> newSet = newItems == null ? Collections.emptySet() :
                (newItems instanceof Set<T> s ? s : new HashSet<>(newItems));

        if (existingSet.isEmpty() && newSet.isEmpty()) {
            return new DiffResult<>(Set.of(), Set.of());
        }

        // 注意：计算 toAdd 和 toDelete 时必须 new 新的 HashSet，以免污染原集合
        Set<T> toAdd = new HashSet<>(newSet);
        toAdd.removeAll(existingSet);

        Set<T> toDelete = new HashSet<>(existingSet);
        toDelete.removeAll(newSet);

        // 返回不可变集合
        return new DiffResult<>(Set.copyOf(toAdd), Set.copyOf(toDelete));
    }

}
