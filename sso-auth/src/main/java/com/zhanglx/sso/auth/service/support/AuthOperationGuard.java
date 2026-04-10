package com.zhanglx.sso.auth.service.support;

import cn.dev33.satoken.stp.StpUtil;
import com.zhanglx.sso.auth.exception.AuthOperationErrorCode;
import com.zhanglx.sso.core.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;

/**
 * 操作保护组件类型。
 */
@Component
public class AuthOperationGuard {

    public Long getCurrentLoginUserId() {
        return StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
    }

    public void checkResetPasswordNotSelf(Long userId) {
        checkUserNotSelf(userId, AuthOperationErrorCode.RESET_CURRENT_USER_PASSWORD_FORBIDDEN);
    }

    public void checkDisableUserNotSelf(Long userId) {
        checkUserNotSelf(userId, AuthOperationErrorCode.DISABLE_CURRENT_USER_FORBIDDEN);
    }

    public void checkDisableUsersNotContainsSelf(Collection<Long> userIds) {
        checkUsersNotContainsSelf(userIds, AuthOperationErrorCode.DISABLE_CURRENT_USER_FORBIDDEN);
    }

    public void checkDeleteUserNotSelf(Long userId) {
        checkUserNotSelf(userId, AuthOperationErrorCode.DELETE_CURRENT_USER_FORBIDDEN);
    }

    public void checkDeleteUsersNotContainsSelf(Collection<Long> userIds) {
        checkUsersNotContainsSelf(userIds, AuthOperationErrorCode.DELETE_CURRENT_USER_FORBIDDEN);
    }

    public void checkRoleUsersBindingDoesNotRemoveCurrentUser(Collection<Long> existingUserIds,
                                                              Collection<Long> targetUserIds) {
        Long currentUserId = getCurrentLoginUserId();
        if (currentUserId == null || existingUserIds == null || !existingUserIds.contains(currentUserId)) {
            return;
        }
        if (targetUserIds == null || !targetUserIds.contains(currentUserId)) {
            throw new BusinessException(AuthOperationErrorCode.REMOVE_CURRENT_USER_ROLE_BINDING_FORBIDDEN);
        }
    }

    /**
     * 校验目标用户不是当前登录人。
     */
    private void checkUserNotSelf(Long userId, AuthOperationErrorCode errorCode) {
        Long currentUserId = getCurrentLoginUserId();
        if (currentUserId != null && Objects.equals(currentUserId, userId)) {
            throw new BusinessException(errorCode);
        }
    }

    /**
     * 校验目标用户集合不包含当前登录人。
     */
    private void checkUsersNotContainsSelf(Collection<Long> userIds, AuthOperationErrorCode errorCode) {
        Long currentUserId = getCurrentLoginUserId();
        if (currentUserId != null && userIds != null && userIds.contains(currentUserId)) {
            throw new BusinessException(errorCode);
        }
    }

}