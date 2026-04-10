package com.zhanglx.sso.xss.wrapper;

import com.zhanglx.sso.xss.support.XssInputSource;
import com.zhanglx.sso.xss.support.XssPolicyMode;
import com.zhanglx.sso.xss.support.XssPolicyResolution;
import com.zhanglx.sso.xss.support.XssSanitizationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.*;

/**
 * 对 Query 参数、表单参数和指定请求头做统一包装清洗。
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final XssSanitizationService sanitizationService;
    /**
     * sanitizedHeaderCache。
     */
    private final Map<String, String> sanitizedHeaderCache = new HashMap<>();
    /**
     * sanitizedParameterMap。
     */
    private Map<String, String[]> sanitizedParameterMap;

    public XssHttpServletRequestWrapper(HttpServletRequest request, XssSanitizationService sanitizationService) {
        super(request);
        this.sanitizationService = sanitizationService;
    }

    @Override
    public String getParameter(String name) {
        String[] values = getParameterValues(name);
        return values == null || values.length == 0 ? null : values[0];
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = getSanitizedParameterMap().get(name);
        return values == null ? null : values.clone();
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> source = getSanitizedParameterMap();
        Map<String, String[]> copied = new LinkedHashMap<>(source.size());
        source.forEach((key, value) -> copied.put(key, value == null ? null : value.clone()));
        return copied;
    }

    @Override
    public String getHeader(String name) {
        String headerValue = super.getHeader(name);
        if (!sanitizationService.shouldApplyGlobalProtection()
                || !sanitizationService.getProperties().shouldSanitizeHeader(name)) {
            return headerValue;
        }
        return sanitizedHeaderCache.computeIfAbsent(
                name,
                headerName -> sanitizationService.sanitize(
                        headerValue,
                        headerName,
                        XssPolicyMode.TEXT,
                        XssInputSource.REQUEST_HEADER,
                        (HttpServletRequest) getRequest()
                ).value()
        );
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        List<String> headers = Collections.list(super.getHeaders(name));
        if (!sanitizationService.shouldApplyGlobalProtection()
                || !sanitizationService.getProperties().shouldSanitizeHeader(name)) {
            return Collections.enumeration(headers);
        }
        List<String> sanitizedHeaders = headers.stream()
                .map(headerValue -> sanitizationService.sanitize(
                        headerValue,
                        name,
                        XssPolicyMode.TEXT,
                        XssInputSource.REQUEST_HEADER,
                        (HttpServletRequest) getRequest()
                ).value())
                .toList();
        return Collections.enumeration(sanitizedHeaders);
    }

    /**
     * getSanitizedParameterMap处理逻辑。
     */
    private Map<String, String[]> getSanitizedParameterMap() {
        if (sanitizedParameterMap != null) {
            return sanitizedParameterMap;
        }

        HttpServletRequest request = (HttpServletRequest) getRequest();
        XssInputSource inputSource = sanitizationService.resolveInputSource(request, XssInputSource.QUERY_OR_FORM);
        Map<String, String[]> originalMap = super.getParameterMap();
        Map<String, String[]> sanitizedMap = new LinkedHashMap<>(originalMap.size());
        originalMap.forEach((parameterName, parameterValues) -> {
            if (parameterValues == null) {
                sanitizedMap.put(parameterName, null);
                return;
            }
            XssPolicyResolution resolution = sanitizationService.resolvePolicy(
                    parameterName,
                    null,
                    XssPolicyMode.TEXT,
                    request
            );
            String[] sanitizedValues = new String[parameterValues.length];
            for (int index = 0; index < parameterValues.length; index++) {
                sanitizedValues[index] = sanitizationService.sanitize(
                        parameterValues[index],
                        parameterName,
                        resolution.mode(),
                        inputSource,
                        request
                ).value();
            }
            sanitizedMap.put(parameterName, sanitizedValues);
        });
        this.sanitizedParameterMap = sanitizedMap;
        return sanitizedMap;
    }
}