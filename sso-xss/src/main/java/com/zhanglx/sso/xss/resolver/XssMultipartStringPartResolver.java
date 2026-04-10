package com.zhanglx.sso.xss.resolver;

import com.zhanglx.sso.xss.support.XssInputSource;
import com.zhanglx.sso.xss.support.XssPolicyMode;
import com.zhanglx.sso.xss.support.XssPolicyResolution;
import com.zhanglx.sso.xss.support.XssSanitizationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.multipart.support.MultipartResolutionDelegate;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 专门处理 multipart/form-data 中的文本分片。
 * `RequestPart` 字符串参数不会稳定经过统一请求体增强链路，因此这里补一层轻量解析器。
 */
@Component
@RequiredArgsConstructor
public class XssMultipartStringPartResolver implements HandlerMethodArgumentResolver {

    private final XssSanitizationService sanitizationService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestPart.class)
                && String.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  @Nullable ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  @Nullable org.springframework.web.bind.support.WebDataBinderFactory binderFactory)
            throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            return null;
        }
        if (sanitizationService.shouldSkipRequest(request)
                || sanitizationService.shouldIgnoreEndpoint(parameter.getContainingClass(), parameter.getMethod(), request)) {
            return readRawStringPart(parameter, request);
        }

        String partName = resolvePartName(parameter);
        MultipartRequest multipartRequest = MultipartResolutionDelegate.resolveMultipartRequest(webRequest);
        XssPolicyResolution resolution = sanitizationService.resolvePolicy(
                partName,
                parameter.getParameter(),
                XssPolicyMode.TEXT,
                request
        );
        String rawValue = readRequiredStringPart(
                partName,
                request,
                multipartRequest,
                parameter.getParameterAnnotation(RequestPart.class)
        );
        return sanitizationService.sanitize(
                rawValue,
                partName,
                resolution.mode(),
                XssInputSource.MULTIPART_PART,
                request
        ).value();
    }

    private String readRawStringPart(MethodParameter parameter, HttpServletRequest request)
            throws Exception {
        String partName = resolvePartName(parameter);
        return readRequiredStringPart(
                partName,
                request,
                MultipartResolutionDelegate.resolveMultipartRequest(new org.springframework.web.context.request.ServletWebRequest(request)),
                parameter.getParameterAnnotation(RequestPart.class)
        );
    }

    private String readRequiredStringPart(String partName,
                                          HttpServletRequest request,
                                          MultipartRequest multipartRequest,
                                          RequestPart requestPart)
            throws Exception {
        MultipartHttpServletRequest multipartHttpServletRequest =
                multipartRequest instanceof MultipartHttpServletRequest currentMultipartRequest
                        ? currentMultipartRequest
                        : null;
        String parameterValue = multipartHttpServletRequest == null
                ? request.getParameter(partName)
                : multipartHttpServletRequest.getParameter(partName);
        if (parameterValue != null) {
            return parameterValue;
        }
        if (multipartHttpServletRequest != null) {
            MultipartFile multipartFile = multipartHttpServletRequest.getFile(partName);
            if (multipartFile != null) {
                return new String(multipartFile.getBytes(), resolveCharset(multipartFile.getContentType()));
            }
        }
        Part part;
        try {
            part = request.getPart(partName);
        } catch (Exception exception) {
            throw new ServletRequestBindingException("读取 multipart 文本分片失败: " + partName, exception);
        }
        if (part == null) {
            if (requestPart == null || requestPart.required()) {
                throw new MissingServletRequestPartException(partName);
            }
            return null;
        }
        byte[] bytes = FileCopyUtils.copyToByteArray(part.getInputStream());
        return new String(bytes, resolveCharset(part.getContentType()));
    }

    /**
     * 解析分片名称。
     */
    private String resolvePartName(MethodParameter parameter) {
        RequestPart requestPart = parameter.getParameterAnnotation(RequestPart.class);
        if (requestPart != null && StringUtils.hasText(requestPart.name())) {
            return requestPart.name();
        }
        if (requestPart != null && StringUtils.hasText(requestPart.value())) {
            return requestPart.value();
        }
        return parameter.getParameterName();
    }

    /**
     * 解析字符集。
     */
    private Charset resolveCharset(@Nullable String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return StandardCharsets.UTF_8;
        }
        try {
            MediaType mediaType = MediaType.parseMediaType(contentType);
            return mediaType.getCharset() == null ? StandardCharsets.UTF_8 : mediaType.getCharset();
        } catch (Exception ignored) {
            return StandardCharsets.UTF_8;
        }
    }
}
