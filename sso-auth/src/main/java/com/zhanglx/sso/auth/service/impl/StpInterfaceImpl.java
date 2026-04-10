package com.zhanglx.sso.auth.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.zhanglx.sso.auth.domain.dto.RoleDTO;
import com.zhanglx.sso.auth.service.PermissionService;
import com.zhanglx.sso.auth.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private static final String MEMBER_LOGIN_TYPE = "member";
    private static final String PERMISSION_CACHE_PREFIX = "sso:auth:permission:";
    private static final String ROLE_CACHE_PREFIX = "sso:auth:role:";
    private static final long CACHE_EXPIRE_HOURS = 2;

    private final PermissionService permissionService;
    private final RoleService roleService;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        if (MEMBER_LOGIN_TYPE.equals(loginType)) {
            return Collections.emptyList();
        }

        String cacheKey = buildCacheKey(PERMISSION_CACHE_PREFIX, loginType, loginId);
        List<String> cachePermissions = stringRedisTemplate.opsForList().range(cacheKey, 0, -1);
        if (!CollectionUtils.isEmpty(cachePermissions)) {
            return cachePermissions;
        }

        List<String> dbPermissions = permissionService.selectPermissionCodesByUserId(Long.valueOf(loginId.toString()));
        if (!CollectionUtils.isEmpty(dbPermissions)) {
            stringRedisTemplate.opsForList().rightPushAll(cacheKey, dbPermissions);
            stringRedisTemplate.expire(cacheKey, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        }

        return dbPermissions;
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        if (MEMBER_LOGIN_TYPE.equals(loginType)) {
            return Collections.emptyList();
        }

        String cacheKey = buildCacheKey(ROLE_CACHE_PREFIX, loginType, loginId);
        List<String> cacheRoles = stringRedisTemplate.opsForList().range(cacheKey, 0, -1);
        if (!CollectionUtils.isEmpty(cacheRoles)) {
            return cacheRoles;
        }

        List<RoleDTO> roleList = roleService.selectRolesForUser(Long.valueOf(loginId.toString()));
        List<String> roleCodes = roleList.stream()
                .map(RoleDTO::getRoleCode)
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(roleCodes)) {
            stringRedisTemplate.opsForList().rightPushAll(cacheKey, roleCodes);
            stringRedisTemplate.expire(cacheKey, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        }

        return roleCodes;
    }

    /**
     * 构建缓存键。
     */
    private String buildCacheKey(String prefix, String loginType, Object loginId) {
        String resolvedLoginType = (loginType == null || loginType.isBlank()) ? StpUtil.TYPE : loginType;
        return prefix + resolvedLoginType + ":" + loginId;
    }
}
