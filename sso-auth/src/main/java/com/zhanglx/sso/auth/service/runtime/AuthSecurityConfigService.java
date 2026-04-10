package com.zhanglx.sso.auth.service.runtime;

import com.zhanglx.sso.core.config.runtime.SystemConfigKeys;
import com.zhanglx.sso.core.config.runtime.SystemConfigProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 认证安全相关运行时配置服务。
 * 通过这个门面集中收口默认密码和 Argon2 Pepper，避免业务层散落配置键。
 */
@Service
@RequiredArgsConstructor
public class AuthSecurityConfigService {

    private final SystemConfigProvider systemConfigProvider;

    /**
     * 获取系统默认密码。
     */
    public String getDefaultPassword() {
        return systemConfigProvider.getRequiredSensitiveString(SystemConfigKeys.DEFAULT_PASSWORD);
    }

    /**
     * 获取 Argon2 Pepper。
     */
    public String getArgon2Pepper() {
        return systemConfigProvider.getRequiredSensitiveString(SystemConfigKeys.SECURITY_ARGON2_PEPPER);
    }
}
