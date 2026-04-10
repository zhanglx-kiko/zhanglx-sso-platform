package com.zhanglx.sso.sms.service;

import com.zhanglx.sso.sms.model.SmsSendRequest;
import com.zhanglx.sso.sms.model.SmsSendResult;

public interface SmsGateway {

    SmsSendResult send(SmsSendRequest request);
}
