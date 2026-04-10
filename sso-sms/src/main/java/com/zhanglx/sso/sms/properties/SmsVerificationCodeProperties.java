package com.zhanglx.sso.sms.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 短信验证码配置属性。
 */
@Data
@ConfigurationProperties(prefix = "sso.sms.verification-code")
public class SmsVerificationCodeProperties {

    /**
     * 同一手机号再次发送验证码前的最小间隔秒数。
     */
    private long resendIntervalSeconds = 60L;

    /**
     * 是否启用模拟发送模式。
     */
    private boolean mockSendEnabled = true;

    /**
     * 手机号发送统计窗口时长，单位为秒。
     */
    private long phoneWindowSeconds = 1800L;

    /**
     * 手机号在统计窗口内允许发送的最大次数。
     */
    private long phoneWindowMaxSends = 10L;

    /**
     * 同一 IP 每日允许发送的最大次数。
     */
    private long ipDailyMaxSends = 20L;
}
