package com.zhanglx.sso.auth.service.support;

import com.zhanglx.sso.auth.exception.AuthManageErrorCode;
import com.zhanglx.sso.auth.exception.AuthOperationErrorCode;
import com.zhanglx.sso.auth.mapper.DeptMapper;
import com.zhanglx.sso.auth.mapper.DictDataMapper;
import com.zhanglx.sso.auth.mapper.RoleDeptMapper;
import com.zhanglx.sso.auth.mapper.RoleMapper;
import com.zhanglx.sso.auth.mapper.UserAppMapper;
import com.zhanglx.sso.auth.mapper.UserPostMapper;
import com.zhanglx.sso.core.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthReferenceCheckSupportTest {

    @Mock
    private UserPostMapper userPostMapper;
    @Mock
    private UserAppMapper userAppMapper;
    @Mock
    private RoleMapper roleMapper;
    @Mock
    private DeptMapper deptMapper;
    @Mock
    private RoleDeptMapper roleDeptMapper;
    @Mock
    private DictDataMapper dictDataMapper;

    private AuthReferenceCheckSupport authReferenceCheckSupport;

    @BeforeEach
    void setUp() {
        authReferenceCheckSupport = new AuthReferenceCheckSupport(
                userPostMapper,
                userAppMapper,
                roleMapper,
                deptMapper,
                roleDeptMapper,
                dictDataMapper
        );
    }

    @Test
    void shouldExposeConcreteUsernameWhenPostIsStillAssigned() {
        when(userPostMapper.selectFirstUsernameByPostId(10L)).thenReturn("admin");

        BusinessException exception = catchThrowableOfType(
                () -> authReferenceCheckSupport.ensurePostCanDelete(10L, "系统管理员岗位"),
                BusinessException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getMessageKey()).isEqualTo(AuthOperationErrorCode.CURRENT_POST_IS_STILL_ASSIGNED_TO_USERS.getMessageKey());
        assertThat(exception.getArgs()).containsExactly("系统管理员岗位", "admin");
    }

    @Test
    void shouldExposeConcreteRoleWhenAppIsStillOccupied() {
        when(roleMapper.selectFirstRoleNameByAppCode("sso")).thenReturn("超级管理员");

        BusinessException exception = catchThrowableOfType(
                () -> authReferenceCheckSupport.ensureAppCanDelete("sso", "统一认证平台"),
                BusinessException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getMessageKey()).isEqualTo(AuthManageErrorCode.APP_ASSIGNED_TO_ROLE.getMessageKey());
        assertThat(exception.getArgs()).containsExactly("统一认证平台", "超级管理员");
    }
}
