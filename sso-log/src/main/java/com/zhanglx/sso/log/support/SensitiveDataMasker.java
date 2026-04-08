package com.zhanglx.sso.log.support;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * 敏感字段脱敏工具。
 * 日志只保留排障所需的摘要信息，绝不把原始密钥、密码、证件号直接落盘。
 */
@Component
public class SensitiveDataMasker {

    private static final Pattern SENSITIVE_KEY_PATTERN = Pattern.compile(
            "(?i).*(password|pwd|token|secret|authorization|access[-_]?token|refresh[-_]?token|idcard|identity|credential|phone|mobile).*"
    );

    private static final Set<String> FULL_MASK_KEYS = Set.of(
            "password", "pwd", "token", "secret", "authorization", "credential"
    );

    public boolean isSensitiveKey(String key) {
        return StringUtils.hasText(key) && SENSITIVE_KEY_PATTERN.matcher(key).matches();
    }

    public String mask(String key, Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        if (!StringUtils.hasText(text)) {
            return text;
        }
        String normalizedKey = key == null ? "" : key.trim().toLowerCase();
        if (FULL_MASK_KEYS.contains(normalizedKey)) {
            return "******";
        }
        if (normalizedKey.contains("phone") || normalizedKey.contains("mobile")) {
            return maskPhone(text);
        }
        if (normalizedKey.contains("idcard") || normalizedKey.contains("identity")) {
            return maskIdentity(text);
        }
        if (normalizedKey.contains("token") || normalizedKey.contains("secret") || normalizedKey.contains("authorization")) {
            return maskKeepEdges(text, 2, 2);
        }
        return "******";
    }

    private String maskPhone(String value) {
        if (value.length() <= 7) {
            return "******";
        }
        return value.substring(0, 3) + "****" + value.substring(value.length() - 4);
    }

    private String maskIdentity(String value) {
        if (value.length() <= 8) {
            return "******";
        }
        return value.substring(0, 4) + "********" + value.substring(value.length() - 4);
    }

    private String maskKeepEdges(String value, int prefixLength, int suffixLength) {
        if (value.length() <= prefixLength + suffixLength) {
            return "******";
        }
        return value.substring(0, prefixLength) + "******" + value.substring(value.length() - suffixLength);
    }
}
