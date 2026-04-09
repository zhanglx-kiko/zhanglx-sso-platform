package com.zhanglx.sso.xss.wrapper;

import com.zhanglx.sso.xss.config.XssProtectionProperties;
import com.zhanglx.sso.xss.support.XssAuditMetrics;
import com.zhanglx.sso.xss.support.XssAuditRecorder;
import com.zhanglx.sso.xss.support.XssSanitizationService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XssHttpServletRequestWrapperTest {

    @Test
    void shouldSanitizeQueryAndFormParameters() {
        XssHttpServletRequestWrapper wrapper = createWrapper(request -> {
            request.setMethod("POST");
            request.setRequestURI("/apis/v1/auth/users");
            request.addParameter("remark", "<script>alert(1)</script>正常备注");
            request.addParameter("searchKey", "张三<script>alert(1)</script>");
            request.addParameter("password", "<script>alert(1)</script>123456");
        });

        assertEquals("正常备注", wrapper.getParameter("remark"));
        assertEquals("张三", wrapper.getParameter("searchKey"));
        assertEquals("<script>alert(1)</script>123456", wrapper.getParameter("password"));
    }

    @Test
    void shouldSanitizeConfiguredHeadersOnly() {
        XssHttpServletRequestWrapper wrapper = createWrapper(request -> {
            request.setMethod("GET");
            request.setRequestURI("/apis/v1/auth/users");
            request.addHeader("User-Agent", "<img src=x onerror=alert(1)>Mozilla");
            request.addHeader("Authorization", "Bearer <img src=x onerror=alert(1)>");
        });

        assertEquals("Mozilla", wrapper.getHeader("User-Agent"));
        assertEquals("Bearer <img src=x onerror=alert(1)>", wrapper.getHeader("Authorization"));
    }

    private XssHttpServletRequestWrapper createWrapper(Consumer consumer) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        consumer.accept(request);
        XssProtectionProperties properties = new XssProtectionProperties();
        properties.setEnabled(true);
        properties.setGlobalEnabled(true);
        return new XssHttpServletRequestWrapper(
                request,
                new XssSanitizationService(
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
                )
        );
    }

    @FunctionalInterface
    private interface Consumer {
        void accept(MockHttpServletRequest request);
    }
}
