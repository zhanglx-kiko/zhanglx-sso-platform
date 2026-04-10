package com.zhanglx.sso.sms.service;

import com.zhanglx.sso.sms.model.SmsVerificationCodeSendCommand;
import com.zhanglx.sso.sms.model.SmsVerificationCodeSendResult;
import com.zhanglx.sso.sms.model.SmsVerificationCodeVerifyCommand;

public interface SmsVerificationCodeManager {

    SmsVerificationCodeSendResult sendCode(SmsVerificationCodeSendCommand command);

    void verifyCode(SmsVerificationCodeVerifyCommand command);
}
