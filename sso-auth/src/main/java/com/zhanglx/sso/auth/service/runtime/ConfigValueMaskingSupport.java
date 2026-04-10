package com.zhanglx.sso.auth.service.runtime;

import com.zhanglx.sso.auth.domain.po.ConfigPO;
import com.zhanglx.sso.auth.enums.YesNoEnum;
import com.zhanglx.sso.core.config.runtime.SystemConfigKeys;
import com.zhanglx.sso.log.support.SensitiveDataMasker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 系统参数脱敏支撑组件。
 * 敏感值只允许在写入时经过请求体传入，读取时统一返回脱敏占位符。
 */
@Component
@RequiredArgsConstructor
public class ConfigValueMaskingSupport {

    /**
     * 敏感配置统一占位符。
     */
    public static final String MASK_PLACEHOLDER = "******";

    private final SensitiveDataMasker sensitiveDataMasker;

    /**
     * 判断配置是否属于敏感配置。
     */
    public boolean isSensitive(ConfigPO configPO) {
        if (configPO == null) {
            return false;
        }
        return YesNoEnum.YES.matches(configPO.getSensitiveFlag())
                || SystemConfigKeys.SECURITY_ARGON2_PEPPER.equals(configPO.getConfigKey())
                || sensitiveDataMasker.isSensitiveKey(configPO.getConfigKey());
    }

    /**
     * 读取场景统一返回脱敏值。
     */
    public String maskForDisplay(ConfigPO configPO) {
        if (configPO == null || !StringUtils.hasText(configPO.getConfigValue())) {
            return configPO == null ? null : configPO.getConfigValue();
        }
        return isSensitive(configPO) ? MASK_PLACEHOLDER : configPO.getConfigValue();
    }

    /**
     * 编辑敏感配置时，如果前端回传的是占位符，表示沿用原值。
     */
    public boolean shouldKeepOriginalValue(ConfigPO configPO, String candidateValue) {
        return isSensitive(configPO) && MASK_PLACEHOLDER.equals(candidateValue);
    }
}
