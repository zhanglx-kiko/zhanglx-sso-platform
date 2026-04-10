package com.zhanglx.sso.sms.support;

public final class SmsMaskingUtils {

    /**
     * 私有构造方法，禁止外部实例化。
     */
    private SmsMaskingUtils() {
    }

    public static String maskPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank() || phoneNumber.length() < 7) {
            return phoneNumber;
        }
        return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(phoneNumber.length() - 4);
    }
}