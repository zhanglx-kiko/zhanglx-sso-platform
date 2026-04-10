package com.zhanglx.sso.sms.enums;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * SmsSceneType枚举。
 */
public enum SmsSceneType {

    REGISTER("REGISTER", "登录/注册", false, Set.of("LOGIN_REGISTER")),
    CHANGE_BOUND_PHONE("CHANGE_BOUND_PHONE", "修改绑定手机号", true, Set.of("MODIFY_BIND_PHONE")),
    FORGOT_PASSWORD("FORGOT_PASSWORD", "重置密码", false, Set.of("RESET_PASSWORD")),
    BIND_PHONE("BIND_PHONE", "绑定新手机号", true, Set.of("BIND_NEW_PHONE")),
    VERIFY_BIND_PHONE("VERIFY_BIND_PHONE", "验证绑定手机号", true, Set.of("VERIFY_BOUND_PHONE"));
    /**
     * 验证码。
     */
    private final String code;
    /**
     * 说明。
     */
    private final String description;
    /**
     * 会员作用域标记。
     */
    private final boolean memberScoped;
    /**
     * 别名集合。
     */
    private final Set<String> aliases;

    SmsSceneType(String code, String description, boolean memberScoped, Set<String> aliases) {
        this.code = code;
        this.description = description;
        this.memberScoped = memberScoped;
        this.aliases = aliases;
    }

    public static Optional<SmsSceneType> resolve(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return Optional.empty();
        }

        String normalized = rawValue.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(item -> item.code.equals(normalized) || item.aliases.contains(normalized))
                .findFirst();
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isMemberScoped() {
        return memberScoped;
    }
}