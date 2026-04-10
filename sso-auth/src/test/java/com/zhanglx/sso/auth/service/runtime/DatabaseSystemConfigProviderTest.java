package com.zhanglx.sso.auth.service.runtime;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.zhanglx.sso.auth.config.ConfigRuntimeCacheProperties;
import com.zhanglx.sso.auth.domain.po.ConfigPO;
import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.auth.mapper.ConfigMapper;
import com.zhanglx.sso.core.exception.SystemConfigException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseSystemConfigProviderTest {

    @Mock
    private ConfigMapper configMapper;

    private DatabaseSystemConfigProvider provider;

    @BeforeEach
    void setUp() {
        ConfigRuntimeCacheProperties cacheProperties = new ConfigRuntimeCacheProperties();
        cacheProperties.setCacheTtlSeconds(300L);
        provider = new DatabaseSystemConfigProvider(configMapper, cacheProperties);
    }

    @Test
    void shouldCacheConfigUntilManualRefresh() {
        ConfigPO configPO = buildConfig("default.password", "123456", EnableStatusEnum.ENABLED);
        when(configMapper.selectOne(any(SFunction.class), eq("default.password"))).thenReturn(configPO);

        String firstValue = provider.getRequiredString("default.password");
        String secondValue = provider.getRequiredString("default.password");

        assertThat(firstValue).isEqualTo("123456");
        assertThat(secondValue).isEqualTo("123456");
        verify(configMapper, times(1)).selectOne(any(SFunction.class), eq("default.password"));

        provider.refresh("default.password");
        provider.getRequiredString("default.password");
        verify(configMapper, times(2)).selectOne(any(SFunction.class), eq("default.password"));
    }

    @Test
    void shouldThrowClearExceptionWhenConfigMissing() {
        when(configMapper.selectOne(any(SFunction.class), eq("security.argon2.pepper"))).thenReturn(null);

        assertThatThrownBy(() -> provider.getRequiredSensitiveString("security.argon2.pepper"))
                .isInstanceOf(SystemConfigException.class)
                .hasMessageContaining("security.argon2.pepper")
                .hasMessageContaining("缺失");
    }

    @Test
    void shouldTreatDisabledConfigAsUnavailableForOptionalRead() {
        ConfigPO configPO = buildConfig("sms.aliyun.sign-name", "速通互联验证平台", EnableStatusEnum.DISABLED);
        when(configMapper.selectOne(any(SFunction.class), eq("sms.aliyun.sign-name"))).thenReturn(configPO);

        assertThat(provider.getString("sms.aliyun.sign-name")).isEmpty();
        assertThatThrownBy(() -> provider.getRequiredString("sms.aliyun.sign-name"))
                .isInstanceOf(SystemConfigException.class)
                .hasMessageContaining("已停用");
    }

    private ConfigPO buildConfig(String configKey, String configValue, EnableStatusEnum status) {
        ConfigPO configPO = new ConfigPO();
        configPO.setConfigKey(configKey);
        configPO.setConfigValue(configValue);
        configPO.setStatus(status);
        return configPO;
    }
}
