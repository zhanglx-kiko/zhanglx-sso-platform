package com.zhanglx.sso.auth.domain.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "sso.sms.verification-code")
public class MemberVerificationCodeProperties {

    private long expireSeconds = 300L;

    private long resendIntervalSeconds = 60L;

    private int codeLength = 6;

    private boolean mockSendEnabled = true;

    private long phoneWindowSeconds = 1800L;

    private long phoneWindowMaxSends = 10L;

    private long ipDailyMaxSends = 20L;

    private Map<String, String> sceneTemplates = createDefaultSceneTemplates();

/*
    private static Map<String, String> defaultSceneTemplates() {
        Map<String, String> templates = new LinkedHashMap<>();
        templates.put("REGISTER", "【张LX】注册验证码：%s，%d分钟内有效。");
        templates.put("FORGOT_PASSWORD", "【张LX】找回密码验证码：%s，%d分钟内有效。");
        templates.put("BIND_PHONE", "【张LX】绑定手机号验证码：%s，%d分钟内有效。");
        return templates;
    }
*/
    private static Map<String, String> defaultSceneTemplatesAscii() {
        Map<String, String> templates = new LinkedHashMap<>();
        templates.put("REGISTER", "\u3010\u5f20LX\u3011\u6ce8\u518c\u9a8c\u8bc1\u7801\uff1a%s\uff0c%d\u5206\u949f\u5185\u6709\u6548\u3002");
        templates.put("FORGOT_PASSWORD", "\u3010\u5f20LX\u3011\u627e\u56de\u5bc6\u7801\u9a8c\u8bc1\u7801\uff1a%s\uff0c%d\u5206\u949f\u5185\u6709\u6548\u3002");
        templates.put("BIND_PHONE", "\u3010\u5f20LX\u3011\u7ed1\u5b9a\u624b\u673a\u53f7\u9a8c\u8bc1\u7801\uff1a%s\uff0c%d\u5206\u949f\u5185\u6709\u6548\u3002");
        return templates;
    }

    private static Map<String, String> createDefaultSceneTemplates() {
        return defaultSceneTemplatesAscii();
    }
}
