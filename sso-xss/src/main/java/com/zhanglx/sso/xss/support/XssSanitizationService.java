package com.zhanglx.sso.xss.support;

import com.zhanglx.sso.xss.annotation.XssPolicy;
import com.zhanglx.sso.xss.config.XssProtectionProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * XSS 清洗核心服务。
 * 这里统一封装路径匹配、字段策略解析、文本清洗和审计记录，避免逻辑散落在过滤器与请求增强组件中。
 */
@Slf4j
@Getter
@Component
@RequiredArgsConstructor
public class XssSanitizationService {

    private static final Pattern DANGEROUS_BLOCK_PATTERN = Pattern.compile(
            "(?is)<\\s*(script|iframe|object|embed|style|link|meta|svg|math|base|form)[^>]*>.*?<\\s*/\\s*\\1\\s*>"
    );

    private static final Pattern DANGEROUS_SINGLE_TAG_PATTERN = Pattern.compile(
            "(?is)<\\s*(script|iframe|object|embed|link|meta|svg|math|base|img|video|audio|source)[^>]*?/?>"
    );

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("(?is)<\\s*/?\\s*[a-z!][^>]*>");

    private static final Pattern HTML_EVENT_ATTRIBUTE_PATTERN = Pattern.compile("(?is)<[^>]+\\s+on[a-z]+\\s*=");

    private static final Pattern HTML_PROTOCOL_PATTERN = Pattern.compile(
            "(?is)<[^>]+(?:href|src)\\s*=\\s*['\"]?\\s*(?:javascript|vbscript|data)\\s*:"
    );

    private static final Pattern DANGEROUS_PROTOCOL_TEXT_PATTERN = Pattern.compile(
            "(?i)(javascript|vbscript|data)\\s*:"
    );

    private static final Safelist RICH_TEXT_SAFELIST = Safelist.relaxed()
            .addTags("span")
            .addProtocols("a", "href", "http", "https", "mailto")
            .addProtocols("img", "src", "http", "https");
    /**
     * 配置属性。
     */
    private final XssProtectionProperties properties;
    /**
     * 审计记录器。
     */
    private final XssAuditRecorder auditRecorder;
    /**
     * pathMatcher。
     */
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public boolean shouldApplyGlobalProtection() {
        return properties.isEnabled() && properties.isGlobalEnabled();
    }

    public boolean shouldSkipRequest(HttpServletRequest request) {
        if (!shouldApplyGlobalProtection() || request == null) {
            return true;
        }
        String requestPath = request.getRequestURI();
        List<String> whitelistPaths = properties.getWhitelistPaths();
        if (whitelistPaths == null || whitelistPaths.isEmpty()) {
            return false;
        }
        boolean matched = whitelistPaths.stream()
                .filter(StringUtils::hasText)
                .anyMatch(pattern -> pathMatcher.match(pattern.trim(), requestPath));
        if (matched) {
            auditRecorder.recordBypassOnce(request, XssBypassReason.WHITELIST_PATH, requestPath);
        }
        return matched;
    }

    public boolean shouldIgnoreContentType(MediaType mediaType, HttpServletRequest request) {
        if (mediaType == null) {
            return false;
        }
        String actualContentType = mediaType.toString().toLowerCase(Locale.ROOT);
        boolean ignored = properties.getIgnoredContentTypes().stream()
                .filter(StringUtils::hasText)
                .map(item -> item.trim().toLowerCase(Locale.ROOT))
                .anyMatch(pattern -> matchContentType(pattern, actualContentType));
        if (ignored) {
            auditRecorder.recordBypassOnce(request, XssBypassReason.IGNORED_CONTENT_TYPE, actualContentType);
        }
        return ignored;
    }

    public boolean shouldIgnoreEndpoint(AnnotatedElement typeElement,
                                        AnnotatedElement methodElement,
                                        HttpServletRequest request) {
        XssPolicyMode methodPolicy = resolveAnnotationPolicy(methodElement);
        if (methodPolicy == XssPolicyMode.NONE) {
            auditRecorder.recordBypassOnce(request, XssBypassReason.ANNOTATION_NONE, fingerprint(methodElement));
            return true;
        }
        XssPolicyMode typePolicy = resolveAnnotationPolicy(typeElement);
        if (typePolicy == XssPolicyMode.NONE) {
            auditRecorder.recordBypassOnce(request, XssBypassReason.ANNOTATION_NONE, fingerprint(typeElement));
            return true;
        }
        return false;
    }

    public XssPolicyResolution resolvePolicy(String fieldName,
                                             AnnotatedElement annotatedElement,
                                             XssPolicyMode defaultMode,
                                             HttpServletRequest request) {
        XssPolicyMode annotationPolicy = resolveAnnotationPolicy(annotatedElement);
        if (annotationPolicy != null) {
            if (annotationPolicy == XssPolicyMode.NONE) {
                auditRecorder.recordBypass(request, XssBypassReason.ANNOTATION_NONE);
                return XssPolicyResolution.bypass(XssBypassReason.ANNOTATION_NONE);
            }
            return XssPolicyResolution.of(annotationPolicy);
        }

        if (properties.containsWhitelistField(fieldName)) {
            auditRecorder.recordBypass(request, XssBypassReason.WHITELIST_FIELD);
            return XssPolicyResolution.bypass(XssBypassReason.WHITELIST_FIELD);
        }
        if (properties.containsRichTextField(fieldName)) {
            return XssPolicyResolution.of(XssPolicyMode.RICH_TEXT);
        }
        if (properties.containsSearchField(fieldName)) {
            return XssPolicyResolution.of(XssPolicyMode.SEARCH);
        }
        return XssPolicyResolution.of(defaultMode);
    }

    public XssSanitizeResult sanitize(String originalValue,
                                      String fieldName,
                                      XssPolicyMode policyMode,
                                      XssInputSource inputSource,
                                      HttpServletRequest request) {
        if (originalValue == null || policyMode == null || policyMode == XssPolicyMode.NONE) {
            return XssSanitizeResult.unchanged(originalValue);
        }

        String cleanedValue = switch (policyMode) {
            case SEARCH -> sanitizeSearchText(originalValue);
            case RICH_TEXT -> sanitizeRichText(originalValue);
            case TEXT -> sanitizePlainText(originalValue);
            case NONE -> originalValue;
        };

        if (originalValue.equals(cleanedValue)) {
            return XssSanitizeResult.unchanged(originalValue);
        }

        auditRecorder.recordHit(request, inputSource, policyMode);
        if (properties.isLogHit()) {
            log.info(
                    "XSS 防护命中: source={}, path={}, field={}, policy={}, before={}, after={}",
                    inputSource,
                    request == null ? "" : request.getRequestURI(),
                    fieldName,
                    policyMode,
                    abbreviateForLog(originalValue),
                    abbreviateForLog(cleanedValue)
            );
        }
        return XssSanitizeResult.changed(cleanedValue);
    }

    public XssInputSource resolveInputSource(HttpServletRequest request, XssInputSource defaultSource) {
        if (request == null
                || defaultSource == XssInputSource.REQUEST_HEADER
                || defaultSource == XssInputSource.PATH_VARIABLE) {
            return defaultSource;
        }
        String contentType = request.getContentType();
        if (StringUtils.hasText(contentType)
                && contentType.toLowerCase(Locale.ROOT).startsWith(MediaType.MULTIPART_FORM_DATA_VALUE)) {
            return XssInputSource.MULTIPART_PART;
        }
        return defaultSource;
    }

    /**
     * 解析注解策略。
     */
    private XssPolicyMode resolveAnnotationPolicy(AnnotatedElement annotatedElement) {
        if (annotatedElement == null) {
            return null;
        }
        XssPolicy annotation = annotatedElement.getAnnotation(XssPolicy.class);
        return annotation == null ? null : annotation.value();
    }

    /**
     * 匹配内容类型。
     */
    private boolean matchContentType(String configuredPattern, String actualContentType) {
        if (configuredPattern.endsWith("/*")) {
            String prefix = configuredPattern.substring(0, configuredPattern.length() - 1);
            return actualContentType.startsWith(prefix);
        }
        return actualContentType.equals(configuredPattern);
    }

    /**
     * 净化plainText。
     */
    private String sanitizePlainText(String originalValue) {
        if (!shouldSanitizeAsPlainText(originalValue)) {
            return originalValue;
        }
        String cleanedValue = Jsoup.clean(stripDangerousBlocks(originalValue), Safelist.none());
        if (properties.getMode() == XssProtectionProperties.XssRuntimeMode.STRICT) {
            return DANGEROUS_PROTOCOL_TEXT_PATTERN.matcher(cleanedValue).replaceAll("$1&#58;");
        }
        return cleanedValue;
    }

    /**
     * 净化searchText。
     */
    private String sanitizeSearchText(String originalValue) {
        if (!shouldSanitizeAsSearchText(originalValue)) {
            return originalValue;
        }
        return Jsoup.clean(stripDangerousBlocks(originalValue), Safelist.none());
    }

    /**
     * 净化richText。
     */
    private String sanitizeRichText(String originalValue) {
        return Jsoup.clean(originalValue, RICH_TEXT_SAFELIST);
    }

    /**
     * 判断是否需要按纯文本方式清洗。
     */
    private boolean shouldSanitizeAsPlainText(String originalValue) {
        if (!StringUtils.hasText(originalValue)) {
            return false;
        }
        if (DANGEROUS_BLOCK_PATTERN.matcher(originalValue).find()
                || DANGEROUS_SINGLE_TAG_PATTERN.matcher(originalValue).find()
                || HTML_EVENT_ATTRIBUTE_PATTERN.matcher(originalValue).find()
                || HTML_PROTOCOL_PATTERN.matcher(originalValue).find()) {
            return true;
        }
        if (properties.getMode() == XssProtectionProperties.XssRuntimeMode.STRICT
                && DANGEROUS_PROTOCOL_TEXT_PATTERN.matcher(originalValue).find()) {
            return true;
        }
        return HTML_TAG_PATTERN.matcher(originalValue).find();
    }

    /**
     * 判断是否需要按搜索文本方式清洗。
     */
    private boolean shouldSanitizeAsSearchText(String originalValue) {
        if (!StringUtils.hasText(originalValue)) {
            return false;
        }
        return DANGEROUS_BLOCK_PATTERN.matcher(originalValue).find()
                || DANGEROUS_SINGLE_TAG_PATTERN.matcher(originalValue).find()
                || HTML_TAG_PATTERN.matcher(originalValue).find();
    }

    /**
     * 剥离dangerousBlocks。
     */
    private String stripDangerousBlocks(String originalValue) {
        String withoutBlocks = DANGEROUS_BLOCK_PATTERN.matcher(originalValue).replaceAll("");
        return DANGEROUS_SINGLE_TAG_PATTERN.matcher(withoutBlocks).replaceAll("");
    }

    /**
     * 压缩forLog。
     */
    private String abbreviateForLog(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replaceAll("[\\r\\n\\t]+", " ");
        if (normalized.length() <= 120) {
            return normalized;
        }
        return normalized.substring(0, 120) + "...";
    }

    /**
     * 生成特征处理逻辑。
     */
    private String fingerprint(AnnotatedElement annotatedElement) {
        return annotatedElement == null ? "unknown" : annotatedElement.toString();
    }
}
