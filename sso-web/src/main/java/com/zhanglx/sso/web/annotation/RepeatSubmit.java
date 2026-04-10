package com.zhanglx.sso.web.annotation;

import java.lang.annotation.*;

/**
 * 重复提交类型。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatSubmit {

    /**
     * 重复提交判定窗口时长，单位为秒。
     * 小于等于 0 时使用全局默认值。
     */
    long windowSeconds() default -1L;

    /**
     * 参与幂等键计算的自定义表达式。
     * 适合按业务主键、表单字段或当前用户维度隔离重复提交。
     */
    String key() default "";

    /**
     * 注解是否生效的条件表达式。
     * 返回 false 时跳过本次重复提交校验。
     */
    String condition() default "";

    /**
     * 命中重复提交时直接返回的提示文案。
     * 未设置时由系统使用默认提示。
     */
    String message() default "";

    /**
     * 命中重复提交时使用的国际化消息键。
     * 配置后优先于直接写死的提示文案。
     */
    String messageKey() default "";
}
