package com.zhanglx.sso.sms.model;

import com.zhanglx.sso.sms.enums.SmsSceneType;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.Map;

@Getter
@Builder
public class SmsSendRequest {

    private final SmsSceneType sceneType;

    private final String phoneNumber;

    @Singular("templateParam")
    private final Map<String, String> templateParams;

    private final String outId;

    private final String clientIp;

    private final String businessKey;
}
