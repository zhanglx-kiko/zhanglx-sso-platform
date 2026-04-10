package com.zhanglx.sso.xss.annotation;

import com.zhanglx.sso.xss.support.XssPolicyMode;

import java.lang.annotation.*;

/**
 * 标记字段、参数或接口的 XSS 清洗策略。
 * 当前推荐优先标在字段和参数上，类和方法级通常只用于显式放行整条链路。
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XssPolicy {

    XssPolicyMode value() default XssPolicyMode.TEXT;
}