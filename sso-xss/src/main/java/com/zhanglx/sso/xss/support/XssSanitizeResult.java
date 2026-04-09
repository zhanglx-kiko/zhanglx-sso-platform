package com.zhanglx.sso.xss.support;

/**
 * XSS 清洗结果。
 *
 * @param value 清洗后的值
 * @param changed 是否发生变化
 */
public record XssSanitizeResult(String value, boolean changed) {

    public static XssSanitizeResult unchanged(String value) {
        return new XssSanitizeResult(value, false);
    }

    public static XssSanitizeResult changed(String value) {
        return new XssSanitizeResult(value, true);
    }
}
