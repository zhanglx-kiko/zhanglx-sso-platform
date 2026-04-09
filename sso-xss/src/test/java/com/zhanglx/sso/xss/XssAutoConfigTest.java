package com.zhanglx.sso.xss;

import com.zhanglx.sso.xss.filter.XssProtectionFilter;
import com.zhanglx.sso.xss.handler.XssRequestBodyAdvice;
import com.zhanglx.sso.xss.interceptor.XssPathVariableInterceptor;
import com.zhanglx.sso.xss.support.XssAuditMetrics;
import com.zhanglx.sso.xss.support.XssSanitizationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class XssAutoConfigTest {

    private final WebApplicationContextRunner webApplicationContextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(XssAutoConfig.class));

    private final ApplicationContextRunner applicationContextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(XssAutoConfig.class));

    @Test
    void shouldAutoConfigureBeansInServletWebApplication() {
        webApplicationContextRunner
                .withPropertyValues("sso.xss.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(XssSanitizationService.class);
                    assertThat(context).hasSingleBean(XssProtectionFilter.class);
                    assertThat(context).hasSingleBean(XssRequestBodyAdvice.class);
                    assertThat(context).hasSingleBean(XssPathVariableInterceptor.class);
                    assertThat(context).hasSingleBean(XssAuditMetrics.class);
                });
    }

    @Test
    void shouldNotRegisterBeansWhenDisabled() {
        webApplicationContextRunner
                .withPropertyValues("sso.xss.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(XssSanitizationService.class);
                    assertThat(context).doesNotHaveBean(XssProtectionFilter.class);
                });
    }

    @Test
    void shouldNotAutoConfigureInNonWebContext() {
        applicationContextRunner
                .withPropertyValues("sso.xss.enabled=true")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(XssSanitizationService.class);
                    assertThat(context).doesNotHaveBean(XssProtectionFilter.class);
                });
    }
}
