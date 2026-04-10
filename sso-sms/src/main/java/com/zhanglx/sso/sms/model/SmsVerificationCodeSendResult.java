package com.zhanglx.sso.sms.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SmsVerificationCodeSendResult {

    private final String maskedPhoneNumber;

    private final long expireSeconds;

    private final long resendIntervalSeconds;
}
