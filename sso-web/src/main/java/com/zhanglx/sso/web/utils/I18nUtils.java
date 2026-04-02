package com.zhanglx.sso.web.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/16 17:47
 * @ClassName: I18nUtils
 * @Description:
 */
@Slf4j
@Component
public class I18nUtils {

    private final MessageSource messageSource;

    public I18nUtils(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * 获取国际化消息（使用当前请求的语言）
     *
     * @param code 消息代码
     * @return 翻译后的消息
     */
    public String getMessage(String code) {
        // 显式转换为 Object[]，明确表示没有参数，消除编译器歧义
        return getMessage(code, (Object[]) null);
    }

    /**
     * 获取国际化消息（带参数）
     *
     * @param code 消息代码
     * @param args 参数数组
     * @return 翻译后的消息
     */
    public String getMessage(String code, Object... args) {
        try {
            Locale locale = LocaleContextHolder.getLocale();
            return messageSource.getMessage(code, args, locale);
        } catch (Exception e) {
            log.warn("获取国际化消息失败，code: {}, error: {}", code, e.getMessage());
            return code;
        }
    }

    /**
     * 获取国际化消息（指定语言）
     *
     * @param code   消息代码
     * @param locale 指定语言
     * @param args   参数数组
     * @return 翻译后的消息
     */
    public String getMessage(String code, Locale locale, Object... args) {
        try {
            return messageSource.getMessage(code, args, locale);
        } catch (Exception e) {
            log.warn("获取国际化消息失败，code: {}, locale: {}, error: {}", code, locale, e.getMessage());
            return code;
        }
    }

}
