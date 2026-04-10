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

    /**
     * 业务模块名称。
     */
    String module();

    /**
     * 模块内功能名称。
     */
    String feature() default "";

    /**
     * 操作类型。
     */
    String operationType();

    /**
     * 操作名称。
     */
    String operationName();

    /**
     * 操作描述。
     */
    String operationDesc() default "";

    /**
     * 是否采集请求体摘要。
     */
    boolean includeRequestBody() default true;

    /**
     * 是否采集响应体摘要。
     */
    boolean includeResponseBody() default false;

    /**
     * 应用编码。
     */
    String appCode() default "";

    /**
     * 应用名称。
     */
    String appName() default "";

    /**
     * 平台编码。
     */
    String platformCode() default "";

    /**
     * 平台名称。
     */
    String platformName() default "";

    /**
     * 来源系统标识。
     */
    String sourceSystem() default "";
}