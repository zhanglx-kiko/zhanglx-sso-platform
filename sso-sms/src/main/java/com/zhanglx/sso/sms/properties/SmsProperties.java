package com.zhanglx.sso.sms.properties;

import com.zhanglx.sso.sms.enums.SmsProviderType;
import com.zhanglx.sso.sms.enums.SmsSceneType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 短信通道配置属性。
 */
@Data
@ConfigurationProperties(prefix = "sso.sms")
public class SmsProperties {

    /**
     * 短信通渠道配置。
     */
    private final SmsChineseProperties smsChinese = new SmsChineseProperties();
    /**
     * 阿里云短信渠道配置。
     */
    private final AliyunProperties aliyun = new AliyunProperties();
    /**
     * 当前启用的短信渠道编码。
     */
    private String provider = SmsProviderType.SMS_CHINESE.getCode();
    /**
     * 是否记录第三方原始响应摘要。
     */
    private boolean logResponseEnabled = true;
    /**
     * 各短信场景的模板配置。
     */
    private Map<String, TemplateProperties> templates = createDefaultTemplates();

    /**
     * 创建默认短信模板映射。
     */
    private static Map<String, TemplateProperties> createDefaultTemplates() {
        Map<String, TemplateProperties> templates = new LinkedHashMap<>();
        templates.put(SmsSceneType.REGISTER.getCode(), createTemplate(
                "登录/注册模板",
                "100001",
                "您的验证码为${code}。尊敬的客户，以上验证码${min}分钟内有效，请注意保密，切勿告知他人。"
        ));
        templates.put(SmsSceneType.CHANGE_BOUND_PHONE.getCode(), createTemplate(
                "修改绑定手机号模板",
                "100002",
                "尊敬的客户，您正在进行修改手机号操作，您的验证码为${code}。以上验证码${min}分钟内有效，请注意保密，切勿告知他人。"
        ));
        templates.put(SmsSceneType.FORGOT_PASSWORD.getCode(), createTemplate(
                "重置密码模板",
                "100003",
                "尊敬的客户，您正在进行重置密码操作，您的验证码为${code}。以上验证码${min}分钟内有效，请注意保密，切勿告知他人。"
        ));
        templates.put(SmsSceneType.BIND_PHONE.getCode(), createTemplate(
                "绑定新手机号模板",
                "100004",
                "尊敬的客户，您正在进行绑定手机号操作，您的验证码为${code}。以上验证码${min}分钟内有效，请注意保密，切勿告知他人。"
        ));
        templates.put(SmsSceneType.VERIFY_BIND_PHONE.getCode(), createTemplate(
                "验证绑定手机号模板",
                "100005",
                "尊敬的客户，您正在验证绑定手机号操作，您的验证码为${code}。以上验证码${min}分钟内有效，请注意保密，切勿告知他人。"
        ));
        return templates;
    }

    /**
     * 创建短信模板默认配置。
     */
    private static TemplateProperties createTemplate(String description, String templateCode, String smsChineseContent) {
        TemplateProperties template = new TemplateProperties();
        template.setDescription(description);
        template.setTemplateCode(templateCode);
        template.setSmsChineseContent(smsChineseContent);
        return template;
    }

    /**
     * 短信通渠道配置。
     */
    @Data
    public static class SmsChineseProperties {

        /**
         * 是否启用短信通渠道。
         */
        private boolean enabled = true;

        /**
         * 短信通用户标识。
         */
        private String uid;

        /**
         * 短信通密钥。
         */
        private String key;

        /**
         * 短信通发送地址。
         */
        private String sendUrl = "https://utf8api.smschinese.cn/";

        /**
         * 连接超时时间，单位为毫秒。
         */
        private int connectTimeoutMillis = 5000;

        /**
         * 读取超时时间，单位为毫秒。
         */
        private int readTimeoutMillis = 5000;
    }

    /**
     * 阿里云短信渠道配置。
     */
    @Data
    public static class AliyunProperties {

        /**
         * 是否启用阿里云短信渠道。
         */
        private boolean enabled = false;

        /**
         * 阿里云 AccessKey 标识。
         */
        private String accessKeyId;

        /**
         * 阿里云 AccessKey Secret。
         */
        private String accessKeySecret;

        /**
         * 阿里云区域编码。
         */
        private String region = "cn-qingdao";

        /**
         * 阿里云接口域名。
         */
        private String endpoint = "dypnsapi.aliyuncs.com";

        /**
         * 阿里云短信签名。
         */
        private String signName = "速通互联验证平台";

        /**
         * 是否要求渠道返回验证码，生产环境固定为 false。
         */
        private boolean returnVerifyCode = false;

        /**
         * 验证码有效期，单位为秒。
         */
        private long validTime = 180L;

        /**
         * 连接超时时间，单位为秒。
         */
        private long connectTimeoutSeconds = 10L;

        /**
         * 响应超时时间，单位为秒。
         */
        private long responseTimeoutSeconds = 10L;
    }

    /**
     * 单个短信模板配置。
     */
    @Data
    public static class TemplateProperties {

        /**
         * 模板说明。
         */
        private String description;

        /**
         * 渠道模板编码。
         */
        private String templateCode;

        /**
         * 短信通渠道使用的短信正文模板。
         */
        private String smsChineseContent;
    }
}