package com.zhanglx.sso.sms.service.provider;

import com.zhanglx.sso.sms.enums.SmsProviderType;
import com.zhanglx.sso.sms.model.SmsSendRequest;
import com.zhanglx.sso.sms.model.SmsSendResult;
import com.zhanglx.sso.sms.properties.SmsProperties;
import com.zhanglx.sso.sms.service.runtime.SmsChannelRuntimeConfigResolver;
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

/**
 * 短信通渠道发送器。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsChineseChannelSender implements SmsChannelSender {
    /**
     * 短信渠道运行时配置解析器。
     */
    private final SmsChannelRuntimeConfigResolver smsChannelRuntimeConfigResolver;
    /**
     * 短信模板支持组件。
     */
    private final SmsTemplateSupport smsTemplateSupport;

    @Override
    public SmsProviderType providerType() {
        return SmsProviderType.SMS_CHINESE;
    }

    @Override
    public SmsSendResult send(SmsSendRequest request, SmsProperties.TemplateProperties templateProperties) {
        SmsChannelRuntimeConfigResolver.SmsChineseChannelConfig providerConfig = smsChannelRuntimeConfigResolver.getSmsChineseConfig();
        if (!providerConfig.enabled()) {
            return buildFailureResult(request, templateProperties, "CHANNEL_DISABLED", "短信通道未启用", null, null);
        }

        if (!providerConfig.isComplete()) {
            return buildFailureResult(request, templateProperties, "CONFIG_INVALID", "短信通平台配置不完整", null, null);
        }

        String content;
        try {
            /*
             * 短信通没有模板签名渲染能力，这里直接生成最终发送正文。
             * 例如登录/注册场景最终发送内容为：
             * 您的验证码为123456。尊敬的客户，以上验证码3分钟内有效，请注意保密，切勿告知他人。
             * 其他场景会按统一模板配置渲染成各自的最终短信内容。
             */
            content = smsTemplateSupport.renderSmsChineseContent(templateProperties, request.getTemplateParams());
        } catch (RuntimeException e) {
            return buildFailureResult(request, templateProperties, "TEMPLATE_PARAM_INVALID", e.getMessage(), null, null);
        }

        HttpClient httpClient = new HttpClient();
        HttpConnectionManagerParams params = httpClient.getHttpConnectionManager().getParams();
        params.setConnectionTimeout(Math.max(1000, providerConfig.connectTimeoutMillis()));
        params.setSoTimeout(Math.max(1000, providerConfig.readTimeoutMillis()));

        PostMethod postMethod = new PostMethod(providerConfig.sendUrl());
        try {
            postMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            postMethod.getParams().setContentCharset("UTF-8");
            postMethod.setRequestBody(new NameValuePair[]{
                    new NameValuePair("Uid", providerConfig.uid().trim()),
                    new NameValuePair("Key", providerConfig.key().trim()),
                    new NameValuePair("smsMob", request.getPhoneNumber()),
                    // 这里提交的 短信正文 就是上面渲染完成后的最终短信正文。
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

    /**
     * 组装短信通发送失败时的统一返回结果。
     */
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
