package com.zhanglx.sso.core.config.runtime;

/**
 * 系统运行时配置键常量。
 * 这些键统一由数据库维护，业务代码禁止散落硬编码。
 */
public final class SystemConfigKeys {

    /**
     * 私有构造方法，禁止实例化。
     */
    private SystemConfigKeys() {
    }

    /**
     * 默认密码配置。
     */
    public static final String DEFAULT_PASSWORD = "default.password";

    /**
     * 短信通渠道配置。
     */
    public static final String SMS_CHINESE_UID = "sms-chinese.uid";
    public static final String SMS_CHINESE_KEY = "sms-chinese.key";
    public static final String SMS_CHINESE_SEND_URL = "sms-chinese.send-url";

    /**
     * 阿里云短信配置。
     */
    public static final String SMS_ALIYUN_ACCESS_KEY_ID = "sms.aliyun.access-key-id";
    public static final String SMS_ALIYUN_ACCESS_KEY_SECRET = "sms.aliyun.access-key-secret";
    public static final String SMS_ALIYUN_ENDPOINT = "sms.aliyun.endpoint";
    public static final String SMS_ALIYUN_REGION = "sms.aliyun.region";
    public static final String SMS_ALIYUN_SIGN_NAME = "sms.aliyun.sign-name";

    /**
     * 微信小程序配置。
     */
    public static final String WECHAT_MINIAPP_APP_ID = "wechat.miniapp.app-id";
    public static final String WECHAT_MINIAPP_SECRET = "wechat.miniapp.secret";

    /**
     * 安全配置。
     */
    public static final String SECURITY_ARGON2_PEPPER = "security.argon2.pepper";
}
