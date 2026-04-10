package com.zhanglx.sso.auth.service;

import com.zhanglx.sso.sms.enums.SmsSceneType;

/**
 * 会员验证码服务接口。
 */
public interface MemberVerificationCodeService {

    void sendCode(SmsSceneType sceneType, String phoneNumber, Long memberId);

    void verifyCode(SmsSceneType sceneType, String phoneNumber, String verificationCode, Long memberId);
}