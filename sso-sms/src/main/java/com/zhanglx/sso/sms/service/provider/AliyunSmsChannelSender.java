package com.zhanglx.sso.sms.service.provider;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhanglx.sso.sms.enums.SmsProviderType;
import com.zhanglx.sso.sms.model.SmsSendRequest;
import com.zhanglx.sso.sms.model.SmsSendResult;
import com.zhanglx.sso.sms.properties.SmsProperties;
import com.zhanglx.sso.sms.provider.aliyun.AliyunSmsVerifyCodeResponsePayload;
import com.zhanglx.sso.sms.support.SmsMaskingUtils;
import darabonba.core.client.ClientOverrideConfiguration;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 阿里云短信渠道发送器。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AliyunSmsChannelSender implements SmsChannelSender {

    private static final String FIXED_SIGN_NAME = "速通互联验证平台";
    private static final boolean FIXED_RETURN_VERIFY_CODE = false;
    private static final long FIXED_VALID_TIME = 180L;
    /**
     * 短信配置属性。
     */
    private final SmsProperties smsProperties;
    /**
     * 对象映射器。
     */
    private final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * 客户端。
     */
    private volatile AsyncClient client;

    @Override
    public SmsProviderType providerType() {
        return SmsProviderType.ALIYUN;
    }

    @Override
    public SmsSendResult send(SmsSendRequest request, SmsProperties.TemplateProperties templateProperties) {
        SmsProperties.AliyunProperties providerProperties = smsProperties.getAliyun();
        if (!providerProperties.isEnabled()) {
            return buildFailureResult(request, templateProperties, "CHANNEL_DISABLED", "阿里云短信通道未启用", null, null, null);
        }

        if (!StringUtils.hasText(providerProperties.getAccessKeyId())
                || !StringUtils.hasText(providerProperties.getAccessKeySecret())
                || !StringUtils.hasText(providerProperties.getEndpoint())
                || !StringUtils.hasText(providerProperties.getRegion())) {
            return buildFailureResult(request, templateProperties, "CONFIG_INVALID", "阿里云短信配置不完整", null, null, null);
        }

        if (templateProperties == null || !StringUtils.hasText(templateProperties.getTemplateCode())) {
            return buildFailureResult(request, templateProperties, "TEMPLATE_NOT_CONFIGURED", "阿里云模板编号未配置", null, null, null);
        }

        String templateParamJson;
        try {
            /*
             * 阿里云这里提交的是模板参数，最终短信正文由模板编号和参数在平台侧渲染。
             * 例如模板 100001 配合 {"code":"123456","min":"3"} 时，
             * 用户最终收到的短信内容为：
             * 您的验证码为123456。尊敬的客户，以上验证码3分钟内有效，请注意保密，切勿告知他人。
             * 其他场景同样会依据对应 模板编号 渲染成最终短信正文。
             */
            templateParamJson = buildTemplateParamJson(request);
        } catch (RuntimeException e) {
            return buildFailureResult(request, templateProperties, "TEMPLATE_PARAM_INVALID", e.getMessage(), null, null, null);
        }

        try {
            logFixedConfigMismatch(providerProperties);
            // 这里发送的是模板编号和模板参数，用户看到的最终短信正文由阿里云完成模板渲染。
            SendSmsVerifyCodeRequest requestModel = SendSmsVerifyCodeRequest.builder()
                    .signName(FIXED_SIGN_NAME)
                    .templateCode(templateProperties.getTemplateCode())
                    .phoneNumber(request.getPhoneNumber())
                    .templateParam(templateParamJson)
                    .returnVerifyCode(FIXED_RETURN_VERIFY_CODE)
                    .validTime(FIXED_VALID_TIME)
                    .outId(request.getOutId())
                    .build();

            SendSmsVerifyCodeResponse response = getClient().sendSmsVerifyCode(requestModel).get();
            AliyunSmsVerifyCodeResponsePayload payload = AliyunSmsVerifyCodeResponsePayload.from(response);
            boolean success = isSuccess(payload);
            String failureReason = success ? null : resolveFailureReason(payload);

            return SmsSendResult.builder()
                    .providerType(providerType())
                    .sceneType(request.getSceneType())
                    .maskedPhoneNumber(SmsMaskingUtils.maskPhone(request.getPhoneNumber()))
                    .templateCode(templateProperties.getTemplateCode())
                    .success(success)
                    .httpStatus(payload.getHttpStatus())
                    .providerCode(payload.getCode())
                    .providerMessage(payload.getMessage())
                    .requestId(payload.getRequestId())
                    .providerRequestId(payload.getModel() == null ? null : payload.getModel().getRequestId())
                    .bizId(payload.getModel() == null ? null : payload.getModel().getBizId())
                    .outId(payload.getModel() == null ? request.getOutId() : payload.getModel().getOutId())
                    .rawSummary(buildRawSummary(payload))
                    .failureReason(failureReason)
                    .build();
        } catch (Exception e) {
            log.error("调用阿里云短信接口异常，scene={}, phone={}", request.getSceneType().getCode(), SmsMaskingUtils.maskPhone(request.getPhoneNumber()), e);
            return buildFailureResult(request, templateProperties, "CLIENT_EXCEPTION", e.getMessage(), null, null, null);
        }
    }

    @PreDestroy
    public void destroy() {
        if (client != null) {
            client.close();
        }
    }

    /**
     * 获取渠道客户端实例。
     */
    private AsyncClient getClient() {
        if (client != null) {
            return client;
        }

        synchronized (this) {
            if (client == null) {
                SmsProperties.AliyunProperties providerProperties = smsProperties.getAliyun();
                Credential credential = Credential.builder()
                        .accessKeyId(providerProperties.getAccessKeyId())
                        .accessKeySecret(providerProperties.getAccessKeySecret())
                        .build();
                ClientOverrideConfiguration configuration = ClientOverrideConfiguration.create()
                        .setEndpointOverride(providerProperties.getEndpoint())
                        .setConnectTimeout(Duration.ofSeconds(Math.max(1L, providerProperties.getConnectTimeoutSeconds())))
                        .setResponseTimeout(Duration.ofSeconds(Math.max(1L, providerProperties.getResponseTimeoutSeconds())));
                client = AsyncClient.builder()
                        .region(providerProperties.getRegion())
                        .credentialsProvider(StaticCredentialProvider.create(credential))
                        .overrideConfiguration(configuration)
                        .build();
            }
        }
        return client;
    }

    /**
     * 判断当前条件是否成立。
     */
    private boolean isSuccess(AliyunSmsVerifyCodeResponsePayload payload) {
        if (payload == null) {
            return false;
        }

        boolean httpOk = payload.getHttpStatus() != null && payload.getHttpStatus() == 200;
        boolean successFlag = Boolean.TRUE.equals(payload.getSuccess());
        boolean codeOk = "OK".equalsIgnoreCase(payload.getCode());
        boolean messageOk = !StringUtils.hasText(payload.getMessage()) || payload.getMessage().trim().contains("成功");
        return httpOk && successFlag && codeOk && messageOk;
    }

    /**
     * 构建短信模板参数 序列化文本。
     */
    private String buildTemplateParamJson(SmsSendRequest request) {
        Map<String, String> templateParams = request.getTemplateParams();
        if (templateParams == null || templateParams.isEmpty()) {
            throw new IllegalArgumentException("短信模板参数不能为空");
        }

        String code = templateParams.get("code");
        String min = templateParams.get("min");
        if (!StringUtils.hasText(code) || !StringUtils.hasText(min)) {
            throw new IllegalArgumentException("短信模板参数缺少 code 或 min");
        }

        Map<String, String> params = new LinkedHashMap<>();
        params.put("code", code);
        params.put("min", min);
        try {
            return objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("短信模板参数序列化失败", e);
        }
    }

    /**
     * 整理第三方原始响应摘要。
     */
    private String buildRawSummary(AliyunSmsVerifyCodeResponsePayload payload) {
        if (payload == null) {
            return null;
        }
        return "httpStatus=" + payload.getHttpStatus()
                + ", success=" + payload.getSuccess()
                + ", code=" + payload.getCode()
                + ", message=" + payload.getMessage()
                + ", requestId=" + payload.getRequestId()
                + ", accessDeniedDetail=" + payload.getAccessDeniedDetail()
                + ", providerRequestId=" + (payload.getModel() == null ? null : payload.getModel().getRequestId())
                + ", bizId=" + (payload.getModel() == null ? null : payload.getModel().getBizId())
                + ", outId=" + (payload.getModel() == null ? null : payload.getModel().getOutId());
    }

    /**
     * 提炼失败原因，便于日志和异常输出。
     */
    private String resolveFailureReason(AliyunSmsVerifyCodeResponsePayload payload) {
        if (payload == null) {
            return "阿里云短信返回为空";
        }
        if (StringUtils.hasText(payload.getAccessDeniedDetail())) {
            return payload.getMessage() + "，原因：" + payload.getAccessDeniedDetail();
        }
        return payload.getMessage();
    }

    /**
     * 记录内部校验或异常信息。
     */
    private void logFixedConfigMismatch(SmsProperties.AliyunProperties providerProperties) {
        if (!FIXED_SIGN_NAME.equals(providerProperties.getSignName())) {
            log.warn("阿里云短信 signName 已固定为 {}，当前配置值 {} 不生效", FIXED_SIGN_NAME, providerProperties.getSignName());
        }
        if (providerProperties.isReturnVerifyCode() != FIXED_RETURN_VERIFY_CODE) {
            log.warn("阿里云短信 returnVerifyCode 已固定为 {}，当前配置值 {} 不生效", FIXED_RETURN_VERIFY_CODE, providerProperties.isReturnVerifyCode());
        }
        if (providerProperties.getValidTime() != FIXED_VALID_TIME) {
            log.warn("阿里云短信 validTime 已固定为 {}，当前配置值 {} 不生效", FIXED_VALID_TIME, providerProperties.getValidTime());
        }
    }

    /**
     * 组装阿里云发送失败时的统一返回结果。
     */
    private SmsSendResult buildFailureResult(
            SmsSendRequest request,
            SmsProperties.TemplateProperties templateProperties,
            String providerCode,
            String providerMessage,
            Integer httpStatus,
            String requestId,
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
                .requestId(requestId)
                .outId(request.getOutId())
                .rawSummary(rawSummary)
                .failureReason(providerMessage)
                .build();
    }
}
