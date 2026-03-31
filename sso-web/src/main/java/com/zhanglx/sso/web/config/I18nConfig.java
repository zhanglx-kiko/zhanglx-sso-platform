package com.zhanglx.sso.web.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/16 17:47
 * @ClassName: I18nConfig
 * @Description:
 */
@Configuration
public class I18nConfig {

    /**
     * 配置消息源
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        // 设置国际化资源文件路径（支持多个，后面的覆盖前面的）
        messageSource.setBasenames(
                "classpath:i18n/messages",
                "classpath:i18n/common",
                "classpath:i18n/exception"
        );
        // 设置默认编码为 UTF-8
        messageSource.setDefaultEncoding("UTF-8");
        // 设置缓存刷新时间（开发环境设为 -1 代表不缓存，生产环境建议设置合理时间）
        messageSource.setCacheMillis(3600);
        // 设置是否使用父级消息代码
        messageSource.setUseCodeAsDefaultMessage(false);
        return messageSource;
    }

    /**
     * 配置语言解析器（基于请求头 Accept-Language）
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        // 设置默认语言为中文
        resolver.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        return resolver;
    }

}
