package com.zhanglx.sso.log.support;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhanglx.sso.common.result.Result;
import com.zhanglx.sso.log.config.OperationLogProperties;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.time.temporal.TemporalAccessor;
import java.util.*;

/**
 * 请求/响应/异常摘要器。
 * 这里显式做深度、字段数、长度控制，避免把大对象原样序列化到日志系统里。
 */
@Component
@RequiredArgsConstructor
public class OperationLogSummarySanitizer {

    private final ObjectMapper objectMapper;
    private final OperationLogProperties properties;
    private final SensitiveDataMasker sensitiveDataMasker;

    public String summarizeRequest(Signature signature, Object[] args) {
        if (!(signature instanceof CodeSignature codeSignature) || ObjectUtils.isEmpty(args)) {
            return null;
        }
        Map<String, Object> summary = new LinkedHashMap<>();
        String[] parameterNames = codeSignature.getParameterNames();
        for (int i = 0; i < args.length; i++) {
            Object argument = args[i];
            if (shouldIgnore(argument)) {
                continue;
            }
            String parameterName = parameterNames != null && i < parameterNames.length ? parameterNames[i] : "arg" + i;
            if (summary.size() >= properties.getBeanPropertyPreviewSize()) {
                summary.put("_truncated", "参数数量超过上限，已截断");
                break;
            }
            summary.put(parameterName, simplify(parameterName, argument, 0, Collections.newSetFromMap(new IdentityHashMap<>())));
        }
        return truncate(writeJson(summary), properties.getRequestBodyMaxLength());
    }

    public String summarizeResponse(Object result, boolean includeResponseBody) {
        if (result == null) {
            return null;
        }
        Object summary = includeResponseBody
                ? simplify("response", result, 0, Collections.newSetFromMap(new IdentityHashMap<>()))
                : summarizeResponseMeta(result);
        return truncate(writeJson(summary), properties.getResponseSummaryMaxLength());
    }

    public String summarizeQuery(String queryString) {
        if (!StringUtils.hasText(queryString)) {
            return null;
        }
        Map<String, String> sanitized = new LinkedHashMap<>();
        for (String pair : queryString.split("&")) {
            if (!StringUtils.hasText(pair)) {
                continue;
            }
            String[] segments = pair.split("=", 2);
            String key = segments[0];
            String value = segments.length > 1 ? segments[1] : "";
            sanitized.put(key, sensitiveDataMasker.isSensitiveKey(key) ? sensitiveDataMasker.mask(key, value) : truncate(value, 128));
            if (sanitized.size() >= properties.getCollectionPreviewSize()) {
                sanitized.put("_truncated", "查询参数超过上限，已截断");
                break;
            }
        }
        return truncate(writeJson(sanitized), properties.getRequestQueryMaxLength());
    }

    public String summarizeErrorMessage(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        String message = throwable.getMessage();
        if (!StringUtils.hasText(message)) {
            message = throwable.getClass().getSimpleName();
        }
        return truncate(message, properties.getErrorMessageMaxLength());
    }

    public String summarizeStackTrace(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        appendThrowable(builder, throwable, 0);
        return truncate(builder.toString(), properties.getExceptionStackMaxLength());
    }

    public String truncateUserAgent(String userAgent) {
        return truncate(userAgent, properties.getUserAgentMaxLength());
    }

    public Map<String, String> sanitizeExt(Map<String, Object> ext) {
        if (CollectionUtils.isEmpty(ext)) {
            return Collections.emptyMap();
        }
        Map<String, String> sanitized = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : ext.entrySet()) {
            if (!StringUtils.hasText(entry.getKey())) {
                continue;
            }
            if (sanitized.size() >= properties.getExtMaxEntries()) {
                sanitized.put("_truncated", "扩展字段超过上限，已截断");
                break;
            }
            String key = truncate(entry.getKey().trim(), properties.getExtKeyMaxLength());
            Object value = entry.getValue();
            String safeValue = sensitiveDataMasker.isSensitiveKey(key)
                    ? sensitiveDataMasker.mask(key, value)
                    : truncate(writeJson(simplify(key, value, 0, Collections.newSetFromMap(new IdentityHashMap<>()))), properties.getExtValueMaxLength());
            sanitized.put(key, safeValue);
        }
        return sanitized;
    }

    private Object summarizeResponseMeta(Object result) {
        if (result instanceof Result<?> response) {
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("code", response.getCode());
            summary.put("msg", truncate(response.getMsg(), 128));
            summary.put("dataType", response.getData() == null ? null : response.getData().getClass().getSimpleName());
            return summary;
        }
        if (result instanceof IPage<?> page) {
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("type", "Page");
            summary.put("current", page.getCurrent());
            summary.put("size", page.getSize());
            summary.put("total", page.getTotal());
            summary.put("records", page.getRecords() == null ? 0 : page.getRecords().size());
            return summary;
        }
        if (result instanceof Collection<?> collection) {
            return Map.of("type", result.getClass().getSimpleName(), "size", collection.size());
        }
        return Map.of("type", result.getClass().getSimpleName());
    }

    private Object simplify(String fieldName, Object value, int depth, Set<Object> visited) {
        if (value == null) {
            return null;
        }
        if (sensitiveDataMasker.isSensitiveKey(fieldName)) {
            return sensitiveDataMasker.mask(fieldName, value);
        }
        if (isSimpleValue(value)) {
            return value instanceof CharSequence ? truncate(String.valueOf(value), 256) : value;
        }
        if (depth >= 2) {
            return Map.of("type", value.getClass().getSimpleName());
        }
        if (visited.contains(value)) {
            return Map.of("type", value.getClass().getSimpleName(), "circular", true);
        }
        visited.add(value);
        try {
            if (value instanceof Map<?, ?> map) {
                return simplifyMap(map, depth, visited);
            }
            if (value instanceof Collection<?> collection) {
                return simplifyCollection(collection, depth, visited);
            }
            if (value.getClass().isArray()) {
                return simplifyArray(value, depth, visited);
            }
            if (value instanceof IPage<?> page) {
                return summarizeResponseMeta(page);
            }
            if (value instanceof Result<?> result) {
                return summarizeResponseMeta(result);
            }
            if (value instanceof MultipartFile file) {
                return Map.of("type", "MultipartFile", "name", file.getOriginalFilename(), "size", file.getSize());
            }
            return simplifyBean(value, depth, visited);
        } finally {
            visited.remove(value);
        }
    }

    private Map<String, Object> simplifyMap(Map<?, ?> map, int depth, Set<Object> visited) {
        Map<String, Object> result = new LinkedHashMap<>();
        int count = 0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (count >= properties.getCollectionPreviewSize()) {
                result.put("_truncated", "Map 元素超过上限，已截断");
                break;
            }
            String key = Objects.toString(entry.getKey(), "null");
            result.put(key, simplify(key, entry.getValue(), depth + 1, visited));
            count++;
        }
        return result;
    }

    private List<Object> simplifyCollection(Collection<?> collection, int depth, Set<Object> visited) {
        List<Object> result = new ArrayList<>();
        int count = 0;
        for (Object element : collection) {
            if (count >= properties.getCollectionPreviewSize()) {
                result.add("集合元素超过上限，已截断");
                break;
            }
            result.add(simplify("item", element, depth + 1, visited));
            count++;
        }
        return result;
    }

    private List<Object> simplifyArray(Object array, int depth, Set<Object> visited) {
        int length = Array.getLength(array);
        List<Object> result = new ArrayList<>();
        for (int i = 0; i < length && i < properties.getCollectionPreviewSize(); i++) {
            result.add(simplify("item", Array.get(array, i), depth + 1, visited));
        }
        if (length > properties.getCollectionPreviewSize()) {
            result.add("数组元素超过上限，已截断");
        }
        return result;
    }

    private Map<String, Object> simplifyBean(Object bean, int depth, Set<Object> visited) {
        Map<String, Object> result = new LinkedHashMap<>();
        int count = 0;
        try {
            for (PropertyDescriptor descriptor : Introspector.getBeanInfo(bean.getClass(), Object.class).getPropertyDescriptors()) {
                if (descriptor.getReadMethod() == null) {
                    continue;
                }
                if (count >= properties.getBeanPropertyPreviewSize()) {
                    result.put("_truncated", "属性数量超过上限，已截断");
                    break;
                }
                String propertyName = descriptor.getName();
                Object propertyValue = descriptor.getReadMethod().invoke(bean);
                result.put(propertyName, simplify(propertyName, propertyValue, depth + 1, visited));
                count++;
            }
        } catch (Exception ignored) {
            return Map.of("type", bean.getClass().getSimpleName());
        }
        result.putIfAbsent("_type", bean.getClass().getSimpleName());
        return result;
    }

    private boolean isSimpleValue(Object value) {
        return value instanceof Number
                || value instanceof Boolean
                || value instanceof Character
                || value instanceof CharSequence
                || value instanceof UUID
                || value instanceof Enum<?>
                || value instanceof TemporalAccessor;
    }

    private boolean shouldIgnore(Object argument) {
        return argument == null
                || argument instanceof jakarta.servlet.ServletRequest
                || argument instanceof jakarta.servlet.ServletResponse
                || argument instanceof org.springframework.validation.BindingResult
                || argument instanceof java.io.InputStream
                || argument instanceof java.io.OutputStream;
    }

    private void appendThrowable(StringBuilder builder, Throwable throwable, int depth) {
        if (throwable == null || depth > 2) {
            return;
        }
        builder.append(throwable.getClass().getName());
        if (StringUtils.hasText(throwable.getMessage())) {
            builder.append(": ").append(throwable.getMessage());
        }
        builder.append('\n');
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        int limit = Math.min(12, stackTrace.length);
        for (int i = 0; i < limit; i++) {
            builder.append("\tat ").append(stackTrace[i]).append('\n');
        }
        if (stackTrace.length > limit) {
            builder.append("\t... ").append(stackTrace.length - limit).append(" more").append('\n');
        }
        if (throwable.getCause() != null && throwable.getCause() != throwable) {
            builder.append("Caused by: ");
            appendThrowable(builder, throwable.getCause(), depth + 1);
        }
    }

    private String writeJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }

    private String truncate(String value, int maxLength) {
        if (!StringUtils.hasText(value) || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...(truncated)";
    }
}
