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
import java.util.Locale;
import java.util.Map;

/**
 * 统一短信网关。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultSmsGateway implements SmsGateway {

    /**
     * 已注册的短信渠道发送器列表。
     */
    private final List<SmsChannelSender> channelSenders;

    /**
     * 短信配置属性。
     */
    private final SmsProperties smsProperties;

    /**
     * 短信模板支持组件。
     */
    private final SmsTemplateSupport smsTemplateSupport;

    /**
     * 渠道发送器索引。
     */
    private final Map<SmsProviderType, SmsChannelSender> senderIndex = new EnumMap<>(SmsProviderType.class);

    /**
     * 启动阶段确定后的生效短信渠道。
     */
    private SmsProviderType effectiveProviderType;

    @PostConstruct
    public void init() {
        for (SmsChannelSender sender : channelSenders) {
            senderIndex.put(sender.providerType(), sender);
        }

        effectiveProviderType = resolveProviderTypeOrThrow();
        log.info("短信网关初始化完成，configuredProvider={}, smsChineseEnabled={}, aliyunEnabled={}, registeredProviders={}, effectiveProvider={}",
                smsProperties.getProvider(),
                smsProperties.getSmsChinese().isEnabled(),
                smsProperties.getAliyun().isEnabled(),
                senderIndex.keySet().stream().map(SmsProviderType::getCode).toList(),
                effectiveProviderType.getCode());
    }

    @Override
    public SmsSendResult send(SmsSendRequest request) {
        if (request == null || request.getSceneType() == null || request.getPhoneNumber() == null) {
            throw BusinessException.internalError("technical.sms.request.invalid");
        }

        SmsProviderType providerType = effectiveProviderType;
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

    /**
     * 启动时解析并校验最终生效的短信渠道。
     */
    private SmsProviderType resolveProviderTypeOrThrow() {
        String rawProvider = smsProperties.getProvider();
        if (isAutoProvider(rawProvider)) {
            return resolveAutoProviderOrThrow();
        }

        SmsProviderType configuredProvider = SmsProviderType.resolve(rawProvider)
                .orElseThrow(() -> new IllegalStateException("短信渠道配置错误：sso.sms.provider=" + rawProvider + " 不受支持"));
        if (!isProviderEnabled(configuredProvider)) {
            throw new IllegalStateException("短信渠道配置错误：sso.sms.provider=" + configuredProvider.getCode() + "，但该渠道未启用");
        }
        if (!senderIndex.containsKey(configuredProvider)) {
            throw new IllegalStateException("短信渠道配置错误：provider=" + configuredProvider.getCode() + " 对应的发送器未注册");
        }
        return configuredProvider;
    }

    /**
     * 自动模式下解析唯一可用的短信渠道。
     */
    private SmsProviderType resolveAutoProviderOrThrow() {
        SmsProviderType enabledProvider = null;
        for (SmsProviderType providerType : SmsProviderType.values()) {
            if (!isProviderEnabled(providerType)) {
                continue;
            }
            if (!senderIndex.containsKey(providerType)) {
                throw new IllegalStateException("短信渠道配置错误：provider=" + providerType.getCode() + " 已启用，但对应的发送器未注册");
            }
            if (enabledProvider != null) {
                throw new IllegalStateException("短信渠道配置错误：sso.sms.provider=auto 时检测到多个已启用渠道，请显式指定 provider");
            }
            enabledProvider = providerType;
        }
        if (enabledProvider == null) {
            throw new IllegalStateException("短信渠道配置错误：sso.sms.provider=auto 时至少需要启用一个短信渠道");
        }
        return enabledProvider;
    }

    /**
     * 判断指定渠道在当前配置下是否被启用。
     */
    private boolean isProviderEnabled(SmsProviderType providerType) {
        return switch (providerType) {
            case SMS_CHINESE -> smsProperties.getSmsChinese().isEnabled();
            case ALIYUN -> smsProperties.getAliyun().isEnabled();
        };
    }

    /**
     * 判断当前 provider 是否启用了自动选择模式。
     */
    private boolean isAutoProvider(String rawProvider) {
        return rawProvider == null
                || rawProvider.isBlank()
                || SmsProperties.AUTO_PROVIDER.equals(rawProvider.trim().toLowerCase(Locale.ROOT));
    }
}
