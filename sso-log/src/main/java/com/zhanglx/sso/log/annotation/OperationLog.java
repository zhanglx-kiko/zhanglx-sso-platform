package com.zhanglx.sso.log.annotation;

import java.lang.annotation.*;

/**
 * 声明式操作日志注解。
 * 切面只做轻量采集和异步投递，真正的 ES 写入在后台线程执行。
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    String module();

    String feature() default "";

    String operationType();

    String operationName();

    String operationDesc() default "";

    boolean includeRequestBody() default true;

    boolean includeResponseBody() default false;

    String appCode() default "";

    String appName() default "";

    String platformCode() default "";

    String platformName() default "";

    String sourceSystem() default "";
}
