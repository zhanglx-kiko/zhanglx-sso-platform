package com.zhanglx.sso.sms.service.runtime;

import com.zhanglx.sso.core.config.runtime.SystemConfigKeys;
import com.zhanglx.sso.core.config.runtime.SystemConfigProvider;
import com.zhanglx.sso.sms.properties.SmsProperties;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class SmsChannelRuntimeConfigResolverTest {

    @Test
    void shouldResolveSmsChineseConfigFromUnifiedProvider() {
        SystemConfigProvider systemConfigProvider = Mockito.mock(SystemConfigProvider.class);
        when(systemConfigProvider.getString(SystemConfigKeys.SMS_CHINESE_UID)).thenReturn(Optional.of("zhanglx"));
        when(systemConfigProvider.getSensitiveString(SystemConfigKeys.SMS_CHINESE_KEY)).thenReturn(Optional.of("secret-key"));
        when(systemConfigProvider.getString(SystemConfigKeys.SMS_CHINESE_SEND_URL)).thenReturn(Optional.of("https://utf8api.smschinese.cn/"));

        SmsProperties smsProperties = new SmsProperties();
        smsProperties.getSmsChinese().setEnabled(true);
        smsProperties.getSmsChinese().setConnectTimeoutMillis(3000);
        smsProperties.getSmsChinese().setReadTimeoutMillis(4000);

        SmsChannelRuntimeConfigResolver resolver = new SmsChannelRuntimeConfigResolver(smsProperties, systemConfigProvider);
        SmsChannelRuntimeConfigResolver.SmsChineseChannelConfig config = resolver.getSmsChineseConfig();

        assertThat(config.enabled()).isTrue();
        assertThat(config.uid()).isEqualTo("zhanglx");
        assertThat(config.key()).isEqualTo("secret-key");
        assertThat(config.sendUrl()).isEqualTo("https://utf8api.smschinese.cn/");
        assertThat(config.connectTimeoutMillis()).isEqualTo(3000);
        assertThat(config.readTimeoutMillis()).isEqualTo(4000);
        assertThat(config.isComplete()).isTrue();
    }

    @Test
    void shouldResolveAliyunConfigFromUnifiedProvider() {
        SystemConfigProvider systemConfigProvider = Mockito.mock(SystemConfigProvider.class);
        when(systemConfigProvider.getString(SystemConfigKeys.SMS_ALIYUN_ACCESS_KEY_ID)).thenReturn(Optional.of("ak"));
        when(systemConfigProvider.getSensitiveString(SystemConfigKeys.SMS_ALIYUN_ACCESS_KEY_SECRET)).thenReturn(Optional.of("sk"));
        when(systemConfigProvider.getString(SystemConfigKeys.SMS_ALIYUN_ENDPOINT)).thenReturn(Optional.of("dypnsapi.aliyuncs.com"));
        when(systemConfigProvider.getString(SystemConfigKeys.SMS_ALIYUN_REGION)).thenReturn(Optional.of("cn-qingdao"));
        when(systemConfigProvider.getString(SystemConfigKeys.SMS_ALIYUN_SIGN_NAME)).thenReturn(Optional.of("速通互联验证平台"));

        SmsProperties smsProperties = new SmsProperties();
        smsProperties.getAliyun().setEnabled(true);
        smsProperties.getAliyun().setReturnVerifyCode(false);
        smsProperties.getAliyun().setValidTime(180);
        smsProperties.getAliyun().setConnectTimeoutSeconds(8);
        smsProperties.getAliyun().setResponseTimeoutSeconds(9);

        SmsChannelRuntimeConfigResolver resolver = new SmsChannelRuntimeConfigResolver(smsProperties, systemConfigProvider);
        SmsChannelRuntimeConfigResolver.AliyunChannelConfig config = resolver.getAliyunConfig();

        assertThat(config.enabled()).isTrue();
        assertThat(config.signName()).isEqualTo("速通互联验证平台");
        assertThat(config.endpoint()).isEqualTo("dypnsapi.aliyuncs.com");
        assertThat(config.region()).isEqualTo("cn-qingdao");
        assertThat(config.connectTimeoutSeconds()).isEqualTo(8);
        assertThat(config.responseTimeoutSeconds()).isEqualTo(9);
        assertThat(config.isComplete()).isTrue();
        assertThat(config.clientFingerprint()).isNotBlank();
    }
}
