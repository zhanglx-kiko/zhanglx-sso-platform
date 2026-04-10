package com.zhanglx.sso.auth.service.support;

import com.zhanglx.sso.auth.exception.AuthManageErrorCode;
import com.zhanglx.sso.auth.exception.AuthOperationErrorCode;
import com.zhanglx.sso.auth.mapper.DeptMapper;
import com.zhanglx.sso.auth.mapper.DictDataMapper;
import com.zhanglx.sso.auth.mapper.RoleDeptMapper;
import com.zhanglx.sso.auth.mapper.RoleMapper;
import com.zhanglx.sso.auth.mapper.UserAppMapper;
import com.zhanglx.sso.auth.mapper.UserPostMapper;
import com.zhanglx.sso.core.utils.AssertUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 统一收口删除前的占用校验，避免业务层散落硬编码错误信息。
 */
@Component
@RequiredArgsConstructor
public class AuthReferenceCheckSupport {

    private final UserPostMapper userPostMapper;
    private final UserAppMapper userAppMapper;
    private final RoleMapper roleMapper;
    private final DeptMapper deptMapper;
    private final RoleDeptMapper roleDeptMapper;
    private final DictDataMapper dictDataMapper;

    public void ensurePostCanDelete(Long postId, String postName) {
        String username = userPostMapper.selectFirstUsernameByPostId(postId);
        AssertUtils.isTrue(username == null, AuthOperationErrorCode.CURRENT_POST_IS_STILL_ASSIGNED_TO_USERS, postName, username);
    }

    public void ensureAppCanDelete(String appCode, String appName) {
        String username = userAppMapper.selectFirstUsernameByAppCode(appCode);
        AssertUtils.isTrue(username == null, AuthManageErrorCode.APP_ASSIGNED_TO_USER, appName, username);

        String roleName = roleMapper.selectFirstRoleNameByAppCode(appCode);
        AssertUtils.isTrue(roleName == null, AuthManageErrorCode.APP_ASSIGNED_TO_ROLE, appName, roleName);
    }

    public void ensureDeptCanDelete(Long deptId, String deptName) {
        String childDeptName = deptMapper.selectFirstChildDeptName(deptId);
        AssertUtils.isTrue(childDeptName == null, AuthManageErrorCode.DEPT_HAS_CHILDREN, deptName, childDeptName);

        String username = deptMapper.selectFirstUsernameByDeptId(deptId);
        AssertUtils.isTrue(username == null, AuthManageErrorCode.DEPT_HAS_USERS, deptName, username);

        String roleName = roleDeptMapper.selectFirstRoleNameByDeptId(deptId);
        AssertUtils.isTrue(roleName == null, AuthManageErrorCode.DEPT_BOUND_TO_ROLE_SCOPE, deptName, roleName);
    }

    public void ensureDictTypeCanDelete(String dictType) {
        String dictLabel = dictDataMapper.selectFirstDictLabelByDictType(dictType);
        AssertUtils.isTrue(dictLabel == null, AuthManageErrorCode.DICT_TYPE_HAS_DATA, dictType, dictLabel);
    }
}
