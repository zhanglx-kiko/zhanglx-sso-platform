package com.zhanglx.sso.auth.service.runtime;

import com.zhanglx.sso.auth.exception.WechatErrorCode;
import com.zhanglx.sso.core.config.runtime.SystemConfigKeys;
import com.zhanglx.sso.core.config.runtime.SystemConfigProvider;
import com.zhanglx.sso.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 微信小程序运行时配置服务。
 * 统一从系统参数中读取小程序登录所需配置，避免业务层散落配置键。
 */
@Service
@RequiredArgsConstructor
public class AuthWechatConfigService {

    private final SystemConfigProvider systemConfigProvider;

    /**
     * 获取微信小程序 AppId。
     */
    public String getMiniappAppId() {
        return getRequiredConfig(SystemConfigKeys.WECHAT_MINIAPP_APP_ID, false);
    }

    /**
     * 获取微信小程序 Secret。
     */
    public String getMiniappSecret() {
        return getRequiredConfig(SystemConfigKeys.WECHAT_MINIAPP_SECRET, true);
    }

    private String getRequiredConfig(String configKey, boolean sensitive) {
        String configValue = (sensitive ? systemConfigProvider.getSensitiveString(configKey) : systemConfigProvider.getString(configKey))
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new BusinessException(WechatErrorCode.WECHAT_MINIAPP_CONFIG_MISSING));
        return configValue.trim();
    }
}
