package com.zhanglx.sso.sms.service;

import com.zhanglx.sso.sms.model.SmsSendRequest;
import com.zhanglx.sso.sms.model.SmsSendResult;

/**
 * SmsGateway接口。
 */
public interface SmsGateway {

    SmsSendResult send(SmsSendRequest request);
}