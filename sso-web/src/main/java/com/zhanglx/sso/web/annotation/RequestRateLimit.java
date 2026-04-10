package com.zhanglx.sso.web.annotation;

import java.lang.annotation.*;

/**
 * 请求限流类型。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestRateLimit {

    long limit() default -1L;

    long windowSeconds() default -1L;

    RateLimitDimension[] dimensions() default {RateLimitDimension.IP, RateLimitDimension.URI};

    String customKey() default "";

    String condition() default "";

    String message() default "";

    String messageKey() default "";

    boolean writeHeaders() default true;
}