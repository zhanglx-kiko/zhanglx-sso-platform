package com.zhanglx.sso.core.strategy;

/**
 * 树结构环处理策略。
 *
 * <p>树过滤策略负责决定哪些节点参与组树，
 * 环处理策略负责决定发现循环引用后如何处置。</p>
 */
public enum TreeCycleStrategy {

    /**
     * 不做环检测。
     *
     * <p>适用于数据写入阶段已经严格保证无环，
     * 且查询侧希望尽量降低额外计算开销的场景。</p>
     */
    SKIP_CHECK,

    /**
     * 检测到环后打断当前环上的一条父子关系，并继续组树。
     *
     * <p>适用于后台查询、树展示等需要尽量返回结果，
     * 同时又不希望脏数据把整棵树拖垮的场景。</p>
     */
    BREAK_AND_CONTINUE,

    /**
     * 检测到环后立即失败。
     *
     * <p>适用于写操作、层级重算、血缘刷新等场景，
     * 一旦发现树数据已损坏，就不应该继续向后执行。</p>
     */
    FAIL_FAST;

    /**
     * 是否需要执行环检测。
     */
    public boolean shouldCheckCycle() {
        return this != SKIP_CHECK;
    }

    /**
     * 是否需要在检测到环后直接失败。
     */
    public boolean shouldFailFast() {
        return this == FAIL_FAST;
    }
}