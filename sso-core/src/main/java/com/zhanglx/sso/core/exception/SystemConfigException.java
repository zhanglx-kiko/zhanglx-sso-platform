package com.zhanglx.sso.core.exception;

/**
 * 系统运行时配置异常。
 * 只输出配置键和异常原因，避免把敏感值写入日志。
 */
public class SystemConfigException extends IllegalStateException {

    public SystemConfigException(String message) {
        super(message);
    }

    public static SystemConfigException providerMissing(String configKey) {
        return new SystemConfigException("未找到系统运行时配置提供者，无法读取配置键：" + configKey);
    }

    public static SystemConfigException missing(String configKey) {
        return new SystemConfigException("系统运行时配置缺失，请检查 t_sys_config，配置键：" + configKey);
    }

    public static SystemConfigException blank(String configKey) {
        return new SystemConfigException("系统运行时配置为空，请检查 t_sys_config，配置键：" + configKey);
    }

    public static SystemConfigException disabled(String configKey) {
        return new SystemConfigException("系统运行时配置已停用，请检查 t_sys_config，配置键：" + configKey);
    }
}
