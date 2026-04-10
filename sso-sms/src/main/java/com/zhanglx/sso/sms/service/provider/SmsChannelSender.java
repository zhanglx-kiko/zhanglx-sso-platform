package com.zhanglx.sso.sms.service.provider;

import com.zhanglx.sso.sms.enums.SmsProviderType;
import com.zhanglx.sso.sms.model.SmsSendRequest;
import com.zhanglx.sso.sms.model.SmsSendResult;
import com.zhanglx.sso.sms.properties.SmsProperties;

/**
 * 短信渠道发送器接口。
 */
public interface SmsChannelSender {

    SmsProviderType providerType();

    SmsSendResult send(SmsSendRequest request, SmsProperties.TemplateProperties templateProperties);
}