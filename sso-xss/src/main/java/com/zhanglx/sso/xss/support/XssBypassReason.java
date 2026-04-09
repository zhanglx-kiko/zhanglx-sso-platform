package com.zhanglx.sso.xss.support;

/**
 * XSS 放行或跳过原因。
 */
public enum XssBypassReason {

    WHITELIST_PATH("whitelist_path"),

    WHITELIST_FIELD("whitelist_field"),

    ANNOTATION_NONE("annotation_none"),

    IGNORED_CONTENT_TYPE("ignored_content_type");

    private final String metricValue;

    XssBypassReason(String metricValue) {
        this.metricValue = metricValue;
    }

    public String metricValue() {
        return metricValue;
    }
}
