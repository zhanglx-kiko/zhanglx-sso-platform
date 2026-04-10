package com.zhanglx.sso.web.annotation;

/**
 * RateLimitDimension枚举。
 */
public enum RateLimitDimension {

    IP,
    USER_ID,
    TOKEN,
    URI,
    METHOD,
    TENANT_ID
}