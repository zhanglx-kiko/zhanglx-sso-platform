package com.zhanglx.sso.sms.service;

import com.zhanglx.sso.sms.model.SmsVerificationCodeSendCommand;
import com.zhanglx.sso.sms.model.SmsVerificationCodeSendResult;
import com.zhanglx.sso.sms.model.SmsVerificationCodeVerifyCommand;

/**
 * 短信验证码管理器接口。
 */
public interface SmsVerificationCodeManager {

    SmsVerificationCodeSendResult sendCode(SmsVerificationCodeSendCommand command);

    void verifyCode(SmsVerificationCodeVerifyCommand command);
}