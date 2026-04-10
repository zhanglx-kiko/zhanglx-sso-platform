package com.zhanglx.sso.sms.model;

import com.zhanglx.sso.sms.enums.SmsProviderType;
import com.zhanglx.sso.sms.enums.SmsSceneType;
import lombok.Builder;
import lombok.Getter;

/**
 * 统一短信发送结果对象。
 */
@Getter
@Builder
public class SmsSendResult {
    /**
     * 短信渠道类型。
     */
    private final SmsProviderType providerType;
    /**
     * 短信场景类型。
     */
    private final SmsSceneType sceneType;
    /**
     * 脱敏手机号。
     */
    private final String maskedPhoneNumber;
    /**
     * 模板编号。
     */
    private final String templateCode;
    /**
     * 是否发送成功。
     */
    private final boolean success;
    /**
     * HTTP状态码。
     */
    private final Integer httpStatus;
    /**
     * 渠道返回码。
     */
    private final String providerCode;
    /**
     * 渠道返回信息。
     */
    private final String providerMessage;
    /**
     * 请求标识。
     */
    private final String requestId;
    /**
     * 渠道请求标识。
     */
    private final String providerRequestId;
    /**
     * 业务流水号。
     */
    private final String bizId;
    /**
     * 外部流水号。
     */
    private final String outId;
    /**
     * 原始响应摘要。
     */
    private final String rawSummary;
    /**
     * 失败原因。
     */
    private final String failureReason;
}