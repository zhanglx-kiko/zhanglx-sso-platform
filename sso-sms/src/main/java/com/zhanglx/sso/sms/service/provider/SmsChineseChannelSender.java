package com.zhanglx.sso.sms.service.provider;

import com.zhanglx.sso.sms.enums.SmsProviderType;
import com.zhanglx.sso.sms.model.SmsSendRequest;
import com.zhanglx.sso.sms.model.SmsSendResult;
import com.zhanglx.sso.sms.properties.SmsProperties;
import com.zhanglx.sso.sms.support.SmsMaskingUtils;
import com.zhanglx.sso.sms.support.SmsTemplateSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsChineseChannelSender implements SmsChannelSender {

    private final SmsProperties smsProperties;
    private final SmsTemplateSupport smsTemplateSupport;

    @Override
    public SmsProviderType providerType() {
        return SmsProviderType.SMS_CHINESE;
    }

    @Override
    public SmsSendResult send(SmsSendRequest request, SmsProperties.TemplateProperties templateProperties) {
        SmsProperties.SmsChineseProperties providerProperties = smsProperties.getSmsChinese();
        if (!providerProperties.isEnabled()) {
            return buildFailureResult(request, templateProperties, "CHANNEL_DISABLED", "短信通道未启用", null, null);
        }
        if (!StringUtils.hasText(providerProperties.getUid())
                || !StringUtils.hasText(providerProperties.getKey())
                || !StringUtils.hasText(providerProperties.getSendUrl())) {
            return buildFailureResult(request, templateProperties, "CONFIG_INVALID", "短信通平台配置不完整", null, null);
        }

        String content;
        try {
            content = smsTemplateSupport.renderSmsChineseContent(templateProperties, request.getTemplateParams());
        } catch (RuntimeException e) {
            return buildFailureResult(request, templateProperties, "TEMPLATE_PARAM_INVALID", e.getMessage(), null, null);
        }

        HttpClient httpClient = new HttpClient();
        HttpConnectionManagerParams params = httpClient.getHttpConnectionManager().getParams();
        params.setConnectionTimeout(Math.max(1000, providerProperties.getConnectTimeoutMillis()));
        params.setSoTimeout(Math.max(1000, providerProperties.getReadTimeoutMillis()));

        PostMethod postMethod = new PostMethod(providerProperties.getSendUrl());
        try {
            postMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            postMethod.getParams().setContentCharset("UTF-8");
            postMethod.setRequestBody(new NameValuePair[]{
                    new NameValuePair("Uid", providerProperties.getUid().trim()),
                    new NameValuePair("Key", providerProperties.getKey().trim()),
                    new NameValuePair("smsMob", request.getPhoneNumber()),
                    new NameValuePair("smsText", content)
            });

            int httpStatus = httpClient.executeMethod(postMethod);
            String responseBody = postMethod.getResponseBodyAsString();
            if (httpStatus != 200) {
                return buildFailureResult(request, templateProperties, "HTTP_STATUS_INVALID", "短信通返回的 HTTP 状态异常", httpStatus, responseBody);
            }

            int providerCode = parseProviderCode(responseBody);
            if (providerCode > 0) {
                return SmsSendResult.builder()
                        .providerType(providerType())
                        .sceneType(request.getSceneType())
                        .maskedPhoneNumber(SmsMaskingUtils.maskPhone(request.getPhoneNumber()))
                        .templateCode(templateProperties.getTemplateCode())
                        .success(true)
                        .httpStatus(httpStatus)
                        .providerCode(String.valueOf(providerCode))
                        .providerMessage("发送成功")
                        .outId(request.getOutId())
                        .rawSummary("count=" + providerCode)
                        .build();
            }

            return buildFailureResult(request, templateProperties, String.valueOf(providerCode), "短信通返回失败状态", httpStatus, responseBody);
        } catch (IOException e) {
            log.error("调用短信通接口异常，scene={}, phone={}", request.getSceneType().getCode(), SmsMaskingUtils.maskPhone(request.getPhoneNumber()), e);
            return buildFailureResult(request, templateProperties, "CLIENT_EXCEPTION", e.getMessage(), null, null);
        } finally {
            postMethod.releaseConnection();
        }
    }

/**
 * 解析短信通返回状态码。
 */
    private int parseProviderCode(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return Integer.MIN_VALUE;
        }

        try {
            return Integer.parseInt(responseBody.trim());
        } catch (NumberFormatException e) {
            log.warn("解析短信通返回值失败，body={}", responseBody, e);
            return Integer.MIN_VALUE;
        }
    }

    private SmsSendResult buildFailureResult(
            SmsSendRequest request,
            SmsProperties.TemplateProperties templateProperties,
            String providerCode,
            String providerMessage,
            Integer httpStatus,
            String rawSummary
    ) {
        return SmsSendResult.builder()
                .providerType(providerType())
                .sceneType(request.getSceneType())
                .maskedPhoneNumber(SmsMaskingUtils.maskPhone(request.getPhoneNumber()))
                .templateCode(templateProperties == null ? null : templateProperties.getTemplateCode())
                .success(false)
                .httpStatus(httpStatus)
                .providerCode(providerCode)
                .providerMessage(providerMessage)
                .outId(request.getOutId())
                .rawSummary(rawSummary)
                .failureReason(providerMessage)
                .build();
    }
}