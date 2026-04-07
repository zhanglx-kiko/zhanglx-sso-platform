package com.zhanglx.sso.auth.utils;

import com.zhanglx.sso.core.exception.BusinessException;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Controller 层请求 ID 解析工具。
 */
public final class RequestIdUtils {

    private RequestIdUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Long parseId(String rawId) {
        return parseId(rawId, "ID");
    }

    public static Long parseId(String rawId, String fieldName) {
        String value = normalize(rawId);
        if (value == null) {
            throw invalidId(fieldName, rawId, null);
        }

        try {
            long id = Long.parseLong(value);
            if (id <= 0) {
                throw invalidId(fieldName, rawId, null);
            }

            return id;
        } catch (NumberFormatException e) {
            throw invalidId(fieldName, rawId, e);
        }
    }

    public static List<Long> parseIds(List<String> rawIds) {
        return parseIds(rawIds, "ID");
    }

    public static List<Long> parseIds(Collection<String> rawIds, String fieldName) {
        if (rawIds == null || rawIds.isEmpty()) {
            return Collections.emptyList(); // 返回空 Set，交由 Controller 的 Assert 去校验
        }

        return rawIds.stream()
                .map(RequestIdUtils::normalize)
                .filter(Objects::nonNull)
                .map(id -> parseId(id, fieldName))
                .toList();
    }

    /**
     * 支持解析逗号分隔的字符串形式的批量 ID
     * 适用于: @PathVariable String ids (例如 /posts/1,2,3)
     */
    public static List<Long> parseCommaIds(String rawIds, String fieldName) {
        if (!StringUtils.hasText(rawIds)) {
            return Collections.emptyList();
        }

        List<String> idList = Arrays.asList(rawIds.split(","));
        return parseIds(idList, fieldName);
    }

    private static String normalize(String rawId) {
        if (rawId == null) {
            return null;
        }

        String value = rawId.trim();
        return value.isEmpty() ? null : value;
    }

    private static BusinessException invalidId(String fieldName, String rawId, Exception e) {
        String message = String.format("无效的%s: %s", fieldName, Objects.toString(rawId, "null"));
        return e == null
                ? BusinessException.badRequest(message)
                : BusinessException.badRequest(message, e);
    }

}