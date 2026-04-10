package com.zhanglx.sso.sms.model;

import com.zhanglx.sso.sms.enums.SmsSceneType;
import com.zhanglx.sso.sms.enums.SmsVerificationBusinessType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SmsVerificationCodeSendCommand {

    private final SmsVerificationBusinessType businessType;

    private final SmsSceneType sceneType;

    private final String phoneNumber;

    private final String subjectKey;

    private final String clientIp;
}
