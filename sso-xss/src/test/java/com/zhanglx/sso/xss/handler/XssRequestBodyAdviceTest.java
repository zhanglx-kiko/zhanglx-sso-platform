package com.zhanglx.sso.xss.handler;

import com.zhanglx.sso.xss.annotation.XssPolicy;
import com.zhanglx.sso.xss.config.XssProtectionProperties;
import com.zhanglx.sso.xss.support.XssAuditMetrics;
import com.zhanglx.sso.xss.support.XssAuditRecorder;
import com.zhanglx.sso.xss.support.XssPolicyMode;
import com.zhanglx.sso.xss.support.XssSanitizationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XssRequestBodyAdviceTest {

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldSanitizeJsonBodyRecursively() throws NoSuchMethodException {
        XssRequestBodyAdvice advice = createAdvice(properties -> {
        });
        MockHttpServletRequest request = buildRequest("/apis/v1/auth/demo/save");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        DemoRequest body = new DemoRequest();
        body.remark = "<script>alert(1)</script>正常备注";
        body.keyword = "张三<script>alert(1)</script>";
        body.password = "<script>alert(1)</script>123456";
        body.content = "<p>保留段落</p><script>alert(1)</script>";
        body.child = new DemoChild();
        body.child.remark = "<img src=x onerror=alert(1)>子级备注";
        body.ext = new LinkedHashMap<>();
        body.ext.put("memo", "<svg onload=alert(1)></svg>扩展信息");

        MethodParameter parameter = methodParameter("save", DemoRequest.class);
        advice.afterBodyRead(
                body,
                jsonInputMessage(),
                parameter,
                DemoRequest.class,
                MappingJackson2HttpMessageConverter.class
        );

        assertEquals("正常备注", body.remark);
        assertEquals("张三", body.keyword);
        assertEquals("<script>alert(1)</script>123456", body.password);
        assertTrue(body.content.contains("<p>保留段落</p>"));
        assertFalse(body.content.contains("<script"));
        assertEquals("子级备注", body.child.remark);
        assertEquals("扩展信息", body.ext.get("memo"));
    }

    @Test
    void shouldSkipWhitelistedPathForJsonBody() throws NoSuchMethodException {
        XssRequestBodyAdvice advice = createAdvice(properties ->
                properties.getWhitelistPaths().add("/public/**")
        );
        MockHttpServletRequest request = buildRequest("/public/editor/callback");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        DemoRequest body = new DemoRequest();
        body.remark = "<script>alert(1)</script>正常备注";

        MethodParameter parameter = methodParameter("save", DemoRequest.class);
        advice.afterBodyRead(
                body,
                jsonInputMessage(),
                parameter,
                DemoRequest.class,
                MappingJackson2HttpMessageConverter.class
        );

        assertEquals("<script>alert(1)</script>正常备注", body.remark);
    }

    private XssRequestBodyAdvice createAdvice(java.util.function.Consumer<XssProtectionProperties> customizer) {
        XssProtectionProperties properties = new XssProtectionProperties();
        properties.setEnabled(true);
        properties.setGlobalEnabled(true);
        customizer.accept(properties);
        return new XssRequestBodyAdvice(new XssSanitizationService(
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
        ));
    }

    private MockHttpServletRequest buildRequest(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        return request;
    }

    private MockHttpInputMessage jsonInputMessage() {
        MockHttpInputMessage inputMessage = new MockHttpInputMessage("{}".getBytes(StandardCharsets.UTF_8));
        inputMessage.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return inputMessage;
    }

    private MethodParameter methodParameter(String methodName, Class<?> parameterType) throws NoSuchMethodException {
        Method method = DemoController.class.getDeclaredMethod(methodName, parameterType);
        return new MethodParameter(method, 0);
    }

    private static final class DemoController {

        @SuppressWarnings("unused")
        public void save(@RequestBody DemoRequest request) {
        }
    }

    private static final class DemoRequest {
        private String remark;
        private String keyword;
        @XssPolicy(XssPolicyMode.NONE)
        private String password;
        @XssPolicy(XssPolicyMode.RICH_TEXT)
        private String content;
        private DemoChild child;
        private Map<String, Object> ext;
    }

    private static final class DemoChild {
        private String remark;
    }
}
