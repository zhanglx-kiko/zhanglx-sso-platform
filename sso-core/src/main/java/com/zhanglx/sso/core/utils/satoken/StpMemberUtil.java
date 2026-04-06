package com.zhanglx.sso.core.utils.satoken;

import cn.dev33.satoken.stp.StpLogic;

/**
 * @Author: Zhang L X
 * @Create: 2026/4/3 22:55
 * @ClassName: StpMemberUtil
 * @Description: C端（电商会员）多账号体系鉴权工具类
 *  * 隔离策略：使用 "member" 作为前缀，与后台的 "login" 彻底物理隔离
 */
public class StpMemberUtil {

    /**
     * 账号体系标识 (这个标识会自动拼接到 Redis 的 Key 中，如：Authorization:member:token:xxx)
     */
    public static final String TYPE = "member";

    /**
     * 实例化底层的 StpLogic 对象
     */
    private static final StpLogic stpLogic = new StpLogic(TYPE);

    /**
     * 获取当前底层的 StpLogic 对象
     */
    public static StpLogic getStpLogic() {
        return stpLogic;
    }

    // =================== 核心方法代理封装 ===================

    /**
     * 会话登录
     * @param id 账号id，建议有多账号体系时，加上类型前缀，但我们已经通过体系隔离了，直接传 member_id 即可
     */
    public static void login(Object id) {
        stpLogic.login(id);
    }

    /**
     * 会话登录，支持设备标识隔离
     */
    public static void login(Object id, String device) {
        if (device == null || device.isBlank()) {
            stpLogic.login(id);
            return;
        }
        stpLogic.login(id, device);
    }

    /**
     * 判断当前会话是否已经登录
     */
    public static boolean isLogin() {
        return stpLogic.isLogin();
    }

    /**
     * 检验当前会话是否已经登录，如未登录，则抛出 NotLoginException 异常
     */
    public static void checkLogin() {
        stpLogic.checkLogin();
    }

    /**
     * 获取当前登录态的 Token 值
     */
    public static String getTokenValue() {
        return stpLogic.getTokenValue();
    }

    /**
     * 获取当前登录账号的 id
     */
    public static Object getLoginId() {
        return stpLogic.getLoginId();
    }

    /**
     * 获取当前登录账号的 id，并转化为 long 类型 (MyBatis-Plus 审计字段常用)
     */
    public static long getLoginIdAsLong() {
        return stpLogic.getLoginIdAsLong();
    }

    /**
     * 获取当前登录账号的 id，如果未登录返回指定默认值
     */
    public static long getLoginIdAsLong(long defaultValue) {
        if (isLogin()) {
            return stpLogic.getLoginIdAsLong();
        }
        return defaultValue;
    }

    /**
     * 当前会话注销登录
     */
    public static void logout() {
        stpLogic.logout();
    }

    /**
     * 注销指定账号的所有会话
     */
    public static void logout(Object id) {
        stpLogic.logout(id);
    }

    /**
     * 获取当前登录账号的角色列表
     */
    public static java.util.List<String> getRoleList() {
        return stpLogic.getRoleList();
    }

    /**
     * 获取当前登录账号的权限列表
     */
    public static java.util.List<String> getPermissionList() {
        return stpLogic.getPermissionList();
    }

    // 如果后续 C端 也需要角色或权限校验（比如区分普通VIP和高级VIP），可以继续在这里代理 hasRole 等方法
    // public static boolean hasRole(String role) { return stpLogic.hasRole(role); }

}
