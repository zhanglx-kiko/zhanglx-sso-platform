package com.zhanglx.sso.sms.enums;

/**
 * 短信验证码业务类型枚举。
 */
public enum SmsVerificationBusinessType {

    MEMBER("member", "会员验证码"),
    SYS_USER("sys-user", "后台用户验证码");
    /**
     * 验证码。
     */
    private final String code;
    /**
     * 说明。
     */
    private final String description;

    SmsVerificationBusinessType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}