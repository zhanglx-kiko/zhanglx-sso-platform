package com.zhanglx.sso.sms.model;

import com.zhanglx.sso.sms.enums.SmsSceneType;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.Map;

/**
 * 统一短信发送请求对象。
 */
@Getter
@Builder
public class SmsSendRequest {

    /**
     * 短信场景类型。
     */
    private final SmsSceneType sceneType;

    /**
     * 手机号。
     */
    private final String phoneNumber;

    /**
     * 模板参数。
     */
    @Singular("templateParam")
    private final Map<String, String> templateParams;

    /**
     * 外部流水号。
     */
    private final String outId;

    /**
     * 客户端地址。
     */
    private final String clientIp;

    /**
     * 业务键。
     */
    private final String businessKey;
}