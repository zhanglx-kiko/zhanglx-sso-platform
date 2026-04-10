package com.zhanglx.sso.sms.enums;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public enum SmsProviderType {

    SMS_CHINESE("sms-chinese", Set.of("current", "smschinese", "sms_chinese")),
    ALIYUN("aliyun", Set.of("aliyun-dypnsapi", "aliyun_sms"));

    private final String code;
    private final Set<String> aliases;

    SmsProviderType(String code, Set<String> aliases) {
        this.code = code;
        this.aliases = aliases;
    }

    public String getCode() {
        return code;
    }

    public static Optional<SmsProviderType> resolve(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return Optional.empty();
        }

        String normalized = rawValue.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(item -> item.code.equals(normalized) || item.aliases.contains(normalized))
                .findFirst();
    }
}
