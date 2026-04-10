package com.zhanglx.sso.sms.model;

import lombok.Builder;
import lombok.Getter;

/**
 * 短信验证码发送结果对象。
 */
@Getter
@Builder
public class SmsVerificationCodeSendResult {
    /**
     * 脱敏手机号。
     */
    private final String maskedPhoneNumber;
    /**
     * 过期秒数。
     */
    private final long expireSeconds;
    /**
     * 重发间隔秒数。
     */
    private final long resendIntervalSeconds;
}