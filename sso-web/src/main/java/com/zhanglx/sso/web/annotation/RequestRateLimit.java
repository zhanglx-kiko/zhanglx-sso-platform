package com.zhanglx.sso.web.annotation;

import java.lang.annotation.*;

/**
 * 请求限流类型。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestRateLimit {

    /**
     * 单个时间窗口内允许通过的最大请求次数。
     * 小于等于 0 时使用全局默认值。
     */
    long limit() default -1L;

    /**
     * 限流统计窗口时长，单位为秒。
     * 小于等于 0 时使用全局默认值。
     */
    long windowSeconds() default -1L;

    /**
     * 参与限流键计算的维度集合。
     * 默认按客户端地址和请求路径组合限流。
     */
    RateLimitDimension[] dimensions() default {RateLimitDimension.IP, RateLimitDimension.URI};

    /**
     * 额外参与限流键计算的自定义表达式。
     * 适合把用户ID、手机号、业务单号等信息拼进限流键。
     */
    String customKey() default "";

    /**
     * 注解是否生效的条件表达式。
     * 返回 false 时跳过本次限流判断。
     */
    String condition() default "";

    /**
     * 触发限流时直接返回的提示文案。
     * 未设置时由系统使用默认提示。
     */
    String message() default "";

    /**
     * 触发限流时使用的国际化消息键。
     * 配置后优先于直接写死的提示文案。
     */
    String messageKey() default "";

    /**
     * 是否在响应头中写入限流相关信息。
     * 关闭后仍会执行限流，只是不回写剩余次数等信息。
     */
    boolean writeHeaders() default true;
}
