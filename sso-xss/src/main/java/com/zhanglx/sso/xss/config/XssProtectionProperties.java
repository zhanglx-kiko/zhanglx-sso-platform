package com.zhanglx.sso.xss.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 全局 XSS 防护配置。
 */
@Data
@ConfigurationProperties(prefix = "sso.xss")
public class XssProtectionProperties {

    /**
     * 是否启用 XSS 组件。
     * 关闭后 starter 不会注册任何 XSS Bean。
     */
    private boolean enabled = false;

    /**
     * 是否启用全局自动处理。
     * 关闭后保留配置与扩展点，但不再自动拦截和清洗请求。
     */
    private boolean globalEnabled = true;

    /**
     * 是否打印命中日志。
     * 默认关闭，避免恶意请求触发日志风暴。
     */
    private boolean logHit = false;

    /**
     * 清洗模式。
     * RELAXED 优先降低误伤，STRICT 更强调防护强度。
     */
    private XssRuntimeMode mode = XssRuntimeMode.RELAXED;

    /**
     * 完全跳过 XSS 处理的路径，支持 Ant 风格。
     */
    private List<String> whitelistPaths = new ArrayList<>();

    /**
     * 完全放行的字段名。
     * 典型场景是密码、令牌、密钥等认证原文，避免为了防 XSS 改坏凭据。
     */
    private List<String> whitelistFields = new ArrayList<>(List.of(
            "password",
            "oldPassword",
            "newPassword",
            "confirmPassword",
            "credential",
            "token",
            "accessToken",
            "refreshToken",
            "secret",
            "clientSecret",
            "authorization"
    ));

    /**
     * 搜索字段名。
     */
    private List<String> searchFields = new ArrayList<>(List.of(
            "searchKey",
            "keyword",
            "query",
            "q"
    ));

    /**
     * 富文本字段名。
     */
    private List<String> richTextFields = new ArrayList<>();

    /**
     * 需要清洗的请求头。
     * 只处理可控且不会破坏认证语义的文本头。
     */
    private List<String> sanitizeHeaderNames = new ArrayList<>(List.of(
            "User-Agent",
            "Referer",
            "X-Requested-With"
    ));

    /**
     * 需要整体忽略的 Content-Type。
     * 主要是二进制或非结构化大文本，避免把文件流当成可清洗文本。
     */
    private List<String> ignoredContentTypes = new ArrayList<>(List.of(
            "application/octet-stream",
            "application/pdf",
            "image/*",
            "audio/*",
            "video/*"
    ));

    public boolean containsWhitelistField(String fieldName) {
        return containsIgnoreCase(whitelistFields, fieldName);
    }

    public boolean containsSearchField(String fieldName) {
        return containsIgnoreCase(searchFields, fieldName);
    }

    public boolean containsRichTextField(String fieldName) {
        return containsIgnoreCase(richTextFields, fieldName);
    }

    public boolean shouldSanitizeHeader(String headerName) {
        return containsIgnoreCase(sanitizeHeaderNames, headerName);
    }

    /**
     * 判断是否包含忽略大小写。
     */
    private boolean containsIgnoreCase(List<String> configuredValues, String fieldName) {
        if (!StringUtils.hasText(fieldName) || configuredValues == null || configuredValues.isEmpty()) {
            return false;
        }
        String normalized = fieldName.trim().toLowerCase(Locale.ROOT);
        return configuredValues.stream()
                .filter(StringUtils::hasText)
                .map(item -> item.trim().toLowerCase(Locale.ROOT))
                .anyMatch(normalized::equals);
    }

    /**
     * 运行强度模式。
     */
    public enum XssRuntimeMode {
        STRICT,
        RELAXED
    }
}