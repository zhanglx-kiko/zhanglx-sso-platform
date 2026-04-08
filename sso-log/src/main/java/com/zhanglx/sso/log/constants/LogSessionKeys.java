package com.zhanglx.sso.log.constants;

/**
 * 当前登录用户写入 Sa-Token TokenSession 的键。
 * 操作日志和登出日志优先从这里取用户展示信息，避免每次再查数据库。
 */
public final class LogSessionKeys {

    public static final String USERNAME = "log.username";
    public static final String DISPLAY_NAME = "log.displayName";
    public static final String CLIENT_TYPE = "log.clientType";

    private LogSessionKeys() {
        throw new UnsupportedOperationException("Utility class");
    }
}
