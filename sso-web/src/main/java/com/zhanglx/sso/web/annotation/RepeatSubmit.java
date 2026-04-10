package com.zhanglx.sso.web.annotation;

import java.lang.annotation.*;

/**
 * 重复提交类型。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatSubmit {

    long windowSeconds() default -1L;

    String key() default "";

    String condition() default "";

    String message() default "";

    String messageKey() default "";
}