package com.zhanglx.sso.auth.service;

public interface MemberVerificationCodeService {

    void sendCode(String scene, String phoneNumber, Long memberId);

    void verifyCode(String scene, String phoneNumber, String verificationCode, Long memberId);
}
