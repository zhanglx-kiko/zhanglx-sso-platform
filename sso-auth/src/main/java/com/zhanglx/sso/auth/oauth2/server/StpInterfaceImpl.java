package com.zhanglx.sso.auth.oauth2.server;

import cn.dev33.satoken.stp.StpInterface;
import com.zhanglx.sso.auth.domain.dto.RoleDTO;
import com.zhanglx.sso.auth.service.PermissionService;
import com.zhanglx.sso.auth.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/24 15:40
 * @ClassName: StpInterfaceImpl
 * @Description: Sa-Token 权限数据源提供者 (基于 Redis 缓存 + 直连数据库)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    // 缓存 Key 前缀定义
    private static final String PERMISSION_CACHE_PREFIX = "sso:auth:permission:";
    private static final String ROLE_CACHE_PREFIX = "sso:auth:role:";
    // 缓存有效期设计为 2 小时 (通常与 Token 过期时间保持一致或略短)
    private static final long CACHE_EXPIRE_HOURS = 2;
    private final PermissionService permissionService;
    private final RoleService roleService;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 获取账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        String cacheKey = PERMISSION_CACHE_PREFIX + loginId;

        // 1. 优先从 Redis 缓存中获取
        List<String> cachePermissions = stringRedisTemplate.opsForList().range(cacheKey, 0, -1);
        if (!CollectionUtils.isEmpty(cachePermissions)) {
            return cachePermissions;
        }

        // 2. 缓存未命中，查数据库 (鉴权通常过滤出 type >= 2 按钮/接口级权限)
        // 注意：你需要确保 PermissionService 中有类似于 selectPermissionCodesByUserId(Long userId) 的方法
        // 该方法底层应该是：基于 userId -> roleId -> permissionId -> 获取 identification
        List<String> dbPermissions = permissionService.selectPermissionCodesByUserId(Long.valueOf(loginId.toString()));

        // 3. 结果写入 Redis 缓存
        if (!CollectionUtils.isEmpty(dbPermissions)) {
            stringRedisTemplate.opsForList().rightPushAll(cacheKey, dbPermissions);
            stringRedisTemplate.expire(cacheKey, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        }

        return dbPermissions;
    }

    /**
     * 获取账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        String cacheKey = ROLE_CACHE_PREFIX + loginId;

        // 1. 优先从 Redis 缓存中获取
        List<String> cacheRoles = stringRedisTemplate.opsForList().range(cacheKey, 0, -1);
        if (!CollectionUtils.isEmpty(cacheRoles)) {
            return cacheRoles;
        }

        // 2. 缓存未命中，查数据库
        // 根据你在 RoleController 中提供的 selectRolesForUser(userId) 方法提取角色编码
        List<RoleDTO> roleList = roleService.selectRolesForUser(Long.valueOf(loginId.toString()));
        List<String> roleCodes = roleList.stream()
                .map(RoleDTO::getRoleCode)
                .collect(Collectors.toList());

        // 3. 结果写入 Redis 缓存
        if (!CollectionUtils.isEmpty(roleCodes)) {
            stringRedisTemplate.opsForList().rightPushAll(cacheKey, roleCodes);
            stringRedisTemplate.expire(cacheKey, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        }

        return roleCodes;
    }
}