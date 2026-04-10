package com.zhanglx.sso.sms.service.impl;

import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.sms.enums.SmsProviderType;
import com.zhanglx.sso.sms.model.SmsSendRequest;
import com.zhanglx.sso.sms.model.SmsSendResult;
import com.zhanglx.sso.sms.properties.SmsProperties;
import com.zhanglx.sso.sms.service.SmsGateway;
import com.zhanglx.sso.sms.service.provider.SmsChannelSender;
import com.zhanglx.sso.sms.support.SmsBusinessExceptionTranslator;
import com.zhanglx.sso.sms.support.SmsMaskingUtils;
import com.zhanglx.sso.sms.support.SmsTemplateSupport;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultSmsGateway implements SmsGateway {

    private final List<SmsChannelSender> channelSenders;
    private final SmsProperties smsProperties;
    private final SmsTemplateSupport smsTemplateSupport;

    private final Map<SmsProviderType, SmsChannelSender> senderIndex = new EnumMap<>(SmsProviderType.class);

    @PostConstruct
    public void init() {
        for (SmsChannelSender sender : channelSenders) {
            senderIndex.put(sender.providerType(), sender);
        }
    }

    @Override
    public SmsSendResult send(SmsSendRequest request) {
        if (request == null || request.getSceneType() == null || request.getPhoneNumber() == null) {
            throw BusinessException.internalError("technical.sms.request.invalid");
        }

        SmsProviderType providerType = SmsProviderType.resolve(smsProperties.getProvider())
                .orElseThrow(() -> BusinessException.internalError("technical.sms.provider.not.supported"));
        SmsChannelSender sender = senderIndex.get(providerType);
        if (sender == null) {
            throw BusinessException.internalError("technical.sms.provider.not.supported");
        }

        SmsProperties.TemplateProperties template = smsTemplateSupport.getTemplate(request.getSceneType());
        SmsSendResult result = sender.send(request, template);
        if (result == null) {
            throw BusinessException.badGateway("technical.sms.send.failed");
        }

        if (result.isSuccess()) {
            log.info("短信发送成功，provider={}, scene={}, phone={}, templateCode={}, requestId={}, providerRequestId={}, bizId={}, outId={}",
                    providerType.getCode(),
                    request.getSceneType().getCode(),
                    SmsMaskingUtils.maskPhone(request.getPhoneNumber()),
                    result.getTemplateCode(),
                    result.getRequestId(),
                    result.getProviderRequestId(),
                    result.getBizId(),
                    result.getOutId());
            return result;
        }

        if (smsProperties.isLogResponseEnabled()) {
            log.warn("短信发送失败，provider={}, scene={}, phone={}, templateCode={}, providerCode={}, providerMessage={}, requestId={}, providerRequestId={}, bizId={}, outId={}, failureReason={}, rawSummary={}",
                    providerType.getCode(),
                    request.getSceneType().getCode(),
                    SmsMaskingUtils.maskPhone(request.getPhoneNumber()),
                    result.getTemplateCode(),
                    result.getProviderCode(),
                    result.getProviderMessage(),
                    result.getRequestId(),
                    result.getProviderRequestId(),
                    result.getBizId(),
                    result.getOutId(),
                    result.getFailureReason(),
                    result.getRawSummary());
        } else {
            log.warn("短信发送失败，provider={}, scene={}, phone={}, templateCode={}, providerCode={}, providerMessage={}, requestId={}, providerRequestId={}, bizId={}, outId={}, failureReason={}",
                    providerType.getCode(),
                    request.getSceneType().getCode(),
                    SmsMaskingUtils.maskPhone(request.getPhoneNumber()),
                    result.getTemplateCode(),
                    result.getProviderCode(),
                    result.getProviderMessage(),
                    result.getRequestId(),
                    result.getProviderRequestId(),
                    result.getBizId(),
                    result.getOutId(),
                    result.getFailureReason());
        }
        throw SmsBusinessExceptionTranslator.translate(result);
    }
}
