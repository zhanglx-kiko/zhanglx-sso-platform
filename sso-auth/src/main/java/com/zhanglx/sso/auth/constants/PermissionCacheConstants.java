package com.zhanglx.sso.auth.constants;

/**
 * 权限缓存常量。
 * 单独抽成常量类，避免缓存配置、缓存注解和缓存清理监听器各自维护一份字符串。
 */
public final class PermissionCacheConstants {

    /**
     * 权限树缓存名。
     */
    public static final String PERMISSION_TREE_CACHE = "PermissionTree";

    /**
     * 私有构造方法，禁止外部实例化。
     */
    private PermissionCacheConstants() {
    }
}