package com.zhanglx.sso.sms.model;

import com.zhanglx.sso.sms.enums.SmsProviderType;
import com.zhanglx.sso.sms.enums.SmsSceneType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SmsSendResult {

    private final SmsProviderType providerType;

    private final SmsSceneType sceneType;

    private final String maskedPhoneNumber;

    private final String templateCode;

    private final boolean success;

    private final Integer httpStatus;

    private final String providerCode;

    private final String providerMessage;

    private final String requestId;

    private final String providerRequestId;

    private final String bizId;

    private final String outId;

    private final String rawSummary;

    private final String failureReason;
}
