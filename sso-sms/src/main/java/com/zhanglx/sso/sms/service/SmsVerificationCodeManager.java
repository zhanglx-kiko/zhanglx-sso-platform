package com.zhanglx.sso.sms.service;

import com.zhanglx.sso.sms.model.SmsVerificationCodeSendCommand;
import com.zhanglx.sso.sms.model.SmsVerificationCodeSendResult;
import com.zhanglx.sso.sms.model.SmsVerificationCodeVerifyCommand;

/**
 * 短信验证码管理器接口。
 */
public interface SmsVerificationCodeManager {

    SmsVerificationCodeSendResult sendCode(SmsVerificationCodeSendCommand command);

    /**
     * 仅校验验证码，不消费验证码。
     */
    void checkCode(SmsVerificationCodeVerifyCommand command);

    /**
     * 校验验证码并消费验证码。
     */
    void verifyCode(SmsVerificationCodeVerifyCommand command);
}
