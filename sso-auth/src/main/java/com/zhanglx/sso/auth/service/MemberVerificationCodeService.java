package com.zhanglx.sso.auth.service;

import com.zhanglx.sso.sms.enums.SmsSceneType;

public interface MemberVerificationCodeService {

    void sendCode(SmsSceneType sceneType, String phoneNumber, Long memberId);

    void verifyCode(SmsSceneType sceneType, String phoneNumber, String verificationCode, Long memberId);
}
