package com.zhanglx.sso.core.config.runtime;

import com.zhanglx.sso.core.exception.SystemConfigException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * 运行时配置默认自动配置。
 * 当业务系统没有提供数据库实现时，先注册一个安全兜底，避免模块装配失败。
 */
@Configuration(proxyBeanMethods = false)
public class SystemConfigProviderAutoConfig {

    /**
     * 默认兜底实现。
     * 普通读取返回空，必填读取抛出明确异常。
     */
    @Bean
    @ConditionalOnMissingBean(SystemConfigProvider.class)
    public SystemConfigProvider systemConfigProvider() {
        return new SystemConfigProvider() {
            @Override
            public Optional<String> getString(String configKey) {
                return Optional.empty();
            }

            @Override
            public Optional<String> getSensitiveString(String configKey) {
                return Optional.empty();
            }

            @Override
            public String getRequiredString(String configKey) {
                throw SystemConfigException.providerMissing(configKey);
            }

            @Override
            public String getRequiredSensitiveString(String configKey) {
                throw SystemConfigException.providerMissing(configKey);
            }

            @Override
            public void refresh(String configKey) {
                // 默认兜底实现不维护缓存，这里保留空实现即可。
            }

            @Override
            public void refreshAll() {
                // 默认兜底实现不维护缓存，这里保留空实现即可。
            }
        };
    }
}
