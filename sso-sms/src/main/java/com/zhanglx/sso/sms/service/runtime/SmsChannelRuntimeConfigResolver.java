package com.zhanglx.sso.sms.service.runtime;

import com.zhanglx.sso.core.config.runtime.SystemConfigKeys;
import com.zhanglx.sso.core.config.runtime.SystemConfigProvider;
import com.zhanglx.sso.sms.properties.SmsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 短信渠道运行时配置解析器。
 * 将数据库配置和本地非敏感参数拼装成稳定的渠道快照，避免业务代码自己按键查配置。
 */
@Component
@RequiredArgsConstructor
public class SmsChannelRuntimeConfigResolver {

    private final SmsProperties smsProperties;
    private final SystemConfigProvider systemConfigProvider;

    /**
     * 解析短信通渠道配置。
     */
    public SmsChineseChannelConfig getSmsChineseConfig() {
        SmsProperties.SmsChineseProperties properties = smsProperties.getSmsChinese();
        return new SmsChineseChannelConfig(
                properties.isEnabled(),
                systemConfigProvider.getString(SystemConfigKeys.SMS_CHINESE_UID).orElse(null),
                systemConfigProvider.getSensitiveString(SystemConfigKeys.SMS_CHINESE_KEY).orElse(null),
                systemConfigProvider.getString(SystemConfigKeys.SMS_CHINESE_SEND_URL).orElse(null),
                properties.getConnectTimeoutMillis(),
                properties.getReadTimeoutMillis()
        );
    }

    /**
     * 解析阿里云短信渠道配置。
     */
    public AliyunChannelConfig getAliyunConfig() {
        SmsProperties.AliyunProperties properties = smsProperties.getAliyun();
        return new AliyunChannelConfig(
                properties.isEnabled(),
                systemConfigProvider.getString(SystemConfigKeys.SMS_ALIYUN_ACCESS_KEY_ID).orElse(null),
                systemConfigProvider.getSensitiveString(SystemConfigKeys.SMS_ALIYUN_ACCESS_KEY_SECRET).orElse(null),
                systemConfigProvider.getString(SystemConfigKeys.SMS_ALIYUN_ENDPOINT).orElse(null),
                systemConfigProvider.getString(SystemConfigKeys.SMS_ALIYUN_REGION).orElse(null),
                systemConfigProvider.getString(SystemConfigKeys.SMS_ALIYUN_SIGN_NAME).orElse(null),
                properties.isReturnVerifyCode(),
                properties.getValidTime(),
                properties.getConnectTimeoutSeconds(),
                properties.getResponseTimeoutSeconds()
        );
    }

    /**
     * 短信通配置快照。
     */
    public record SmsChineseChannelConfig(
            boolean enabled,
            String uid,
            String key,
            String sendUrl,
            int connectTimeoutMillis,
            int readTimeoutMillis
    ) {
        public boolean isComplete() {
            return StringUtils.hasText(uid)
                    && StringUtils.hasText(key)
                    && StringUtils.hasText(sendUrl);
        }
    }

    /**
     * 阿里云配置快照。
     */
    public record AliyunChannelConfig(
            boolean enabled,
            String accessKeyId,
            String accessKeySecret,
            String endpoint,
            String region,
            String signName,
            boolean returnVerifyCode,
            long validTime,
            long connectTimeoutSeconds,
            long responseTimeoutSeconds
    ) {
        public boolean isComplete() {
            return StringUtils.hasText(accessKeyId)
                    && StringUtils.hasText(accessKeySecret)
                    && StringUtils.hasText(endpoint)
                    && StringUtils.hasText(region)
                    && StringUtils.hasText(signName);
        }

        /**
         * 生成客户端指纹。
         * 只用于判断本地缓存客户端是否需要重建，不会对外输出。
         */
        public String clientFingerprint() {
            return Integer.toHexString(Objects.hash(
                    accessKeyId,
                    accessKeySecret,
                    endpoint,
                    region,
                    signName,
                    returnVerifyCode,
                    validTime,
                    connectTimeoutSeconds,
                    responseTimeoutSeconds
            ));
        }
    }
}
