package com.zhanglx.sso.xss.support;

/**
 * XSS 清洗策略。
 */
public enum XssPolicyMode {

    /**
     * 普通文本，默认不允许携带 HTML。
     */
    TEXT("text"),

    /**
     * 搜索文本，尽量保留原始搜索语义，只清理明显的标签型注入。
     */
    SEARCH("search"),

    /**
     * 富文本，按白名单保留安全标签和属性。
     */
    RICH_TEXT("rich_text"),

    /**
     * 完全跳过 XSS 清洗。
     */
    NONE("none");

    private final String metricValue;

    XssPolicyMode(String metricValue) {
        this.metricValue = metricValue;
    }

    public String metricValue() {
        return metricValue;
    }
}
