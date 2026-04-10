package com.zhanglx.sso.sms.model;

import com.zhanglx.sso.sms.enums.SmsSceneType;
import com.zhanglx.sso.sms.enums.SmsVerificationBusinessType;
import lombok.Builder;
import lombok.Getter;

/**
 * 短信验证码发送命令。
 */
@Getter
@Builder
public class SmsVerificationCodeSendCommand {
    /**
     * 业务类型。
     */
    private final SmsVerificationBusinessType businessType;
    /**
     * 短信场景类型。
     */
    private final SmsSceneType sceneType;
    /**
     * 手机号。
     */
    private final String phoneNumber;
    /**
     * 业务主体标识。
     */
    private final String subjectKey;
    /**
     * 客户端地址。
     */
    private final String clientIp;
}