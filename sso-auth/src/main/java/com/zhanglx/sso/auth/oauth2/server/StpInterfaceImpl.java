package com.zhanglx.sso.auth.oauth2.server;

import cn.dev33.satoken.stp.StpInterface;
import com.zhanglx.sso.auth.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/24 15:40
 * @ClassName: StpInterfaceImpl
 * @Description:
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {
    // 注入你的权限服务或角色服务
    private final PermissionService permissionService;

    /**
     * 返回一个账号所拥有的权限码集合 (主要对应你的 Type=3 和 Type=2 的 identification)
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // todo 获取权限实现接口级别的权限控制
        // 1. 根据 loginId (userId) 查询用户拥有的所有 Role
        // 2. 根据 Role 关联查询所有的 Permission (建议过滤出 type >= 2 的数据，因为平台/菜单对 Sa-Token 拦截无意义)
        // 3. 提取所有的 identification 返回
        // 返回示例: ["user:add", "user:delete", "role:update"]
        return List.of("user:add", "user:delete"); // 替换为实际数据库查询
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 返回该用户对应的 roleCode 列表
        return List.of("admin", "super-admin");
    }


    // -----------------------------------------------------------
    // -----------------------------------------------------------
    // redis获取权限信息
    // -----------------------------------------------------------
    // -----------------------------------------------------------

//    private final StringRedisTemplate stringRedisTemplate;
    // 如果不用 Redis，也可以用 OpenFeign 调 Auth 接口
    // private final AuthFeignClient authFeignClient;

//    @Override
//    public List<String> getPermissionList(Object loginId, String loginType) {
    // 方案 A：直接从 Redis 读取 Auth 服务缓存好的权限列表（推荐，性能最高）
//        String redisKey = "sso:user:permissions:" + loginId;
    // 反序列化 Redis 中的权限集合...
    // return redisTemplate.opsForValue().get(redisKey);

    // 方案 B：通过 RPC (Feign) 调用 Auth 服务获取
    // return authFeignClient.getUserPermissions(Long.valueOf(loginId.toString()));
//        return List.of();
//    }

//    @Override
//    public List<String> getRoleList(Object loginId, String loginType) {
    // 同理，从 Redis 或 Feign 获取角色列表
//        return List.of();
//    }

}
