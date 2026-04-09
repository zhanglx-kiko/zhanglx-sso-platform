package com.zhanglx.sso.xss.support;

import com.zhanglx.sso.xss.config.XssProtectionProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XssSanitizationServiceTest {

    @Test
    void shouldLeaveNormalTextUnchanged() {
        XssSanitizationService service = createService(properties -> {
        });
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/apis/v1/auth/demo");

        XssSanitizeResult result = service.sanitize(
                "正常备注-张三_2026",
                "remark",
                XssPolicyMode.TEXT,
                XssInputSource.QUERY_OR_FORM,
                request
        );

        assertFalse(result.changed());
        assertEquals("正常备注-张三_2026", result.value());
    }

    @Test
    void shouldCleanDangerousPlainTextInStrictMode() {
        XssSanitizationService service = createService(properties ->
                properties.setMode(XssProtectionProperties.XssRuntimeMode.STRICT)
        );
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/apis/v1/auth/demo");

        XssSanitizeResult result = service.sanitize(
                "javascript:alert(1)",
                "remark",
                XssPolicyMode.TEXT,
                XssInputSource.QUERY_OR_FORM,
                request
        );

        assertTrue(result.changed());
        assertEquals("javascript&#58;alert(1)", result.value());
    }

    @Test
    void shouldKeepSafeRichTextAndRemoveScript() {
        XssSanitizationService service = createService(properties -> {
        });
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/apis/v1/auth/editor");

        XssSanitizeResult result = service.sanitize(
                "<p>安全内容</p><script>alert(1)</script><strong>加粗</strong><a href='javascript:alert(1)'>链接</a>",
                "content",
                XssPolicyMode.RICH_TEXT,
                XssInputSource.JSON_BODY,
                request
        );

        assertTrue(result.changed());
        assertTrue(result.value().contains("<p>安全内容</p>"));
        assertTrue(result.value().contains("<strong>加粗</strong>"));
        assertFalse(result.value().contains("<script"));
        assertFalse(result.value().contains("javascript:"));
    }

    @Test
    void shouldSkipWhitelistedPath() {
        XssSanitizationService service = createService(properties ->
                properties.getWhitelistPaths().add("/public/**")
        );
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/public/editor/callback");

        assertTrue(service.shouldSkipRequest(request));
    }

    @Test
    void shouldRespectGlobalSwitch() {
        XssSanitizationService service = createService(properties -> {
            properties.setEnabled(false);
            properties.setGlobalEnabled(true);
        });

        assertFalse(service.shouldApplyGlobalProtection());
    }

    private XssSanitizationService createService(Consumer<XssProtectionProperties> customizer) {
        XssProtectionProperties properties = new XssProtectionProperties();
        properties.setEnabled(true);
        properties.setGlobalEnabled(true);
        customizer.accept(properties);
        return new XssSanitizationService(
                properties,
                new XssAuditRecorder(new XssAuditMetrics(new org.springframework.beans.factory.ObjectProvider<>() {
                    @Override
                    public io.micrometer.core.instrument.MeterRegistry getObject(Object... args) {
                        return null;
                    }

                    @Override
                    public io.micrometer.core.instrument.MeterRegistry getIfAvailable() {
                        return null;
                    }

                    @Override
                    public io.micrometer.core.instrument.MeterRegistry getIfUnique() {
                        return null;
                    }

                    @Override
                    public io.micrometer.core.instrument.MeterRegistry getObject() {
                        return null;
                    }
                }))
        );
    }
}
