package com.zhanglx.sso.auth.event;

import com.zhanglx.sso.core.utils.collection.CollectionDiffUtils;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/20 12:00
 * @ClassName: RoleUsersChangedEvent
 * @Description:
 */
public record RoleUsersChangedEvent(
        Long roleId,
        CollectionDiffUtils.DiffResult<Long> diffResult
) {
}
