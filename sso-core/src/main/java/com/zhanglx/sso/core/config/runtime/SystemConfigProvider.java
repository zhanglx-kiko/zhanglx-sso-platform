package com.zhanglx.sso.core.config.runtime;

import java.util.Optional;

/**
 * 统一的系统运行时配置访问接口。
 * 业务代码只能通过这个接口或其上层封装读取数据库配置。
 */
public interface SystemConfigProvider {

    /**
     * 按键读取普通配置。
     */
    Optional<String> getString(String configKey);

    /**
     * 按键读取敏感配置。
     * 当前实现与普通配置共用一套底层读取逻辑，这个方法主要用于语义收口。
     */
    Optional<String> getSensitiveString(String configKey);

    /**
     * 读取必填普通配置，缺失时抛出清晰异常。
     */
    String getRequiredString(String configKey);

    /**
     * 读取必填敏感配置，缺失时抛出清晰异常。
     */
    String getRequiredSensitiveString(String configKey);

    /**
     * 按键刷新单个配置缓存。
     */
    void refresh(String configKey);

    /**
     * 刷新全部配置缓存。
     */
    void refreshAll();
}
