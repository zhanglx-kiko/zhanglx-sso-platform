package com.zhanglx.sso.web.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
