package com.zhanglx.sso.auth.event;

import com.zhanglx.sso.core.utils.collection.CollectionDiffUtils;

/**
 * 作者：Zhang L X
 * 创建时间：2026/3/20 12:00
 * 类名：RoleUsersChangedEvent
 * 说明：
 */
public record RoleUsersChangedEvent(
        Long roleId,
        CollectionDiffUtils.DiffResult<Long> diffResult
) {
}