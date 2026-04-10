package com.zhanglx.sso.auth.service.runtime;

import com.zhanglx.sso.auth.domain.po.ConfigPO;
import com.zhanglx.sso.auth.enums.YesNoEnum;
import com.zhanglx.sso.log.support.SensitiveDataMasker;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigValueMaskingSupportTest {

    private final ConfigValueMaskingSupport maskingSupport = new ConfigValueMaskingSupport(new SensitiveDataMasker());

    @Test
    void shouldMaskSensitiveConfigForDisplay() {
        ConfigPO configPO = new ConfigPO();
        configPO.setConfigKey("sms.aliyun.access-key-secret");
        configPO.setConfigValue("secret-value");
        configPO.setSensitiveFlag(YesNoEnum.YES);

        assertThat(maskingSupport.maskForDisplay(configPO)).isEqualTo(ConfigValueMaskingSupport.MASK_PLACEHOLDER);
        assertThat(maskingSupport.shouldKeepOriginalValue(configPO, ConfigValueMaskingSupport.MASK_PLACEHOLDER)).isTrue();
    }

    @Test
    void shouldKeepPlainValueForNonSensitiveConfig() {
        ConfigPO configPO = new ConfigPO();
        configPO.setConfigKey("sms.aliyun.endpoint");
        configPO.setConfigValue("dypnsapi.aliyuncs.com");
        configPO.setSensitiveFlag(YesNoEnum.NO);

        assertThat(maskingSupport.maskForDisplay(configPO)).isEqualTo("dypnsapi.aliyuncs.com");
        assertThat(maskingSupport.shouldKeepOriginalValue(configPO, "dypnsapi.aliyuncs.com")).isFalse();
    }
}
