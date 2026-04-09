package com.zhanglx.sso.xss.support;

/**
 * XSS 命中来源。
 */
public enum XssInputSource {

    QUERY_OR_FORM("query_or_form"),

    JSON_BODY("json_body"),

    PATH_VARIABLE("path_variable"),

    REQUEST_HEADER("request_header"),

    MULTIPART_PART("multipart_part");

    private final String metricValue;

    XssInputSource(String metricValue) {
        this.metricValue = metricValue;
    }

    public String metricValue() {
        return metricValue;
    }
}
