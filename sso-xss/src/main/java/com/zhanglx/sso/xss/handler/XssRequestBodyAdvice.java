package com.zhanglx.sso.xss.handler;

import com.zhanglx.sso.xss.support.XssInputSource;
import com.zhanglx.sso.xss.support.XssPolicyMode;
import com.zhanglx.sso.xss.support.XssPolicyResolution;
import com.zhanglx.sso.xss.support.XssSanitizationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 统一清洗 JSON 请求体和 multipart 文本分片。
 * 这里只在反序列化完成后递归处理，避免和 Jackson / MessageConverter 的解析规则互相打架。
 */
@ControllerAdvice(basePackages = "com.zhanglx")
@RequiredArgsConstructor
public class XssRequestBodyAdvice extends RequestBodyAdviceAdapter {

    private final XssSanitizationService sanitizationService;

    private final Map<Class<?>, List<Field>> fieldCache = new ConcurrentHashMap<>();

    @Override
    public boolean supports(MethodParameter methodParameter,
                            Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object afterBodyRead(Object body,
                                HttpInputMessage inputMessage,
                                MethodParameter parameter,
                                Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {
        HttpServletRequest request = currentRequest();
        if (request == null
                || sanitizationService.shouldSkipRequest(request)
                || sanitizationService.shouldIgnoreEndpoint(parameter.getContainingClass(), parameter.getMethod(), request)
                || sanitizationService.shouldIgnoreContentType(inputMessage.getHeaders().getContentType(), request)) {
            return body;
        }

        IdentityHashMap<Object, Boolean> visited = new IdentityHashMap<>();
        XssInputSource inputSource = sanitizationService.resolveInputSource(request, XssInputSource.JSON_BODY);
        sanitizeValue(body, parameter.getParameter(), parameter.getParameterName(), request, inputSource, visited);
        return body;
    }

    @SuppressWarnings("unchecked")
    private Object sanitizeValue(@Nullable Object value,
                                 @Nullable AnnotatedElement annotatedElement,
                                 @Nullable String fieldName,
                                 HttpServletRequest request,
                                 XssInputSource inputSource,
                                 IdentityHashMap<Object, Boolean> visited) {
        if (value == null) {
            return null;
        }

        XssPolicyResolution resolution = sanitizationService.resolvePolicy(
                fieldName,
                annotatedElement,
                XssPolicyMode.TEXT,
                request
        );
        if (resolution.mode() == XssPolicyMode.NONE) {
            return value;
        }

        if (value instanceof CharSequence sequence) {
            return sanitizationService.sanitize(
                    sequence.toString(),
                    fieldName,
                    resolution.mode(),
                    inputSource,
                    request
            ).value();
        }

        if (isSimpleValueType(value.getClass())) {
            return value;
        }

        if (visited.containsKey(value)) {
            return value;
        }
        visited.put(value, Boolean.TRUE);

        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int index = 0; index < length; index++) {
                Object elementValue = Array.get(value, index);
                Object sanitizedElement = sanitizeValue(
                        elementValue,
                        annotatedElement,
                        fieldName,
                        request,
                        inputSource,
                        visited
                );
                if (sanitizedElement != elementValue) {
                    Array.set(value, index, sanitizedElement);
                }
            }
            return value;
        }

        if (value instanceof List<?> list) {
            for (int index = 0; index < list.size(); index++) {
                Object elementValue = list.get(index);
                Object sanitizedElement = sanitizeValue(
                        elementValue,
                        annotatedElement,
                        fieldName,
                        request,
                        inputSource,
                        visited
                );
                if (sanitizedElement != elementValue) {
                    ((List<Object>) list).set(index, sanitizedElement);
                }
            }
            return value;
        }

        if (value instanceof Set<?> set) {
            List<Object> sanitizedValues = new ArrayList<>(set.size());
            boolean changed = false;
            for (Object elementValue : set) {
                Object sanitizedElement = sanitizeValue(
                        elementValue,
                        annotatedElement,
                        fieldName,
                        request,
                        inputSource,
                        visited
                );
                sanitizedValues.add(sanitizedElement);
                changed = changed || sanitizedElement != elementValue;
            }
            if (changed) {
                ((Set<Object>) set).clear();
                ((Set<Object>) set).addAll(sanitizedValues);
            }
            return value;
        }

        if (value instanceof Collection<?> collection) {
            List<Object> sanitizedValues = new ArrayList<>(collection.size());
            boolean changed = false;
            for (Object elementValue : collection) {
                Object sanitizedElement = sanitizeValue(
                        elementValue,
                        annotatedElement,
                        fieldName,
                        request,
                        inputSource,
                        visited
                );
                sanitizedValues.add(sanitizedElement);
                changed = changed || sanitizedElement != elementValue;
            }
            if (changed) {
                ((Collection<Object>) collection).clear();
                ((Collection<Object>) collection).addAll(sanitizedValues);
            }
            return value;
        }

        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) map).entrySet()) {
                Object originalEntryValue = entry.getValue();
                Object sanitizedEntryValue = sanitizeValue(
                        originalEntryValue,
                        annotatedElement,
                        fieldName,
                        request,
                        inputSource,
                        visited
                );
                if (sanitizedEntryValue != originalEntryValue) {
                    ((Map.Entry<Object, Object>) entry).setValue(sanitizedEntryValue);
                }
            }
            return value;
        }

        sanitizeBeanFields(value, request, inputSource, visited);
        return value;
    }

    private void sanitizeBeanFields(Object bean,
                                    HttpServletRequest request,
                                    XssInputSource inputSource,
                                    IdentityHashMap<Object, Boolean> visited) {
        for (Field field : getSanitizableFields(bean.getClass())) {
            try {
                Object fieldValue = field.get(bean);
                Object sanitizedValue = sanitizeValue(fieldValue, field, field.getName(), request, inputSource, visited);
                if (sanitizedValue != fieldValue) {
                    field.set(bean, sanitizedValue);
                }
            } catch (IllegalAccessException ignored) {
                // 字段初始化时已经统一放开访问权限，这里正常不会进入。
            }
        }
    }

    private List<Field> getSanitizableFields(Class<?> beanClass) {
        return fieldCache.computeIfAbsent(beanClass, currentClass -> {
            List<Field> fields = new ArrayList<>();
            Class<?> targetClass = currentClass;
            while (targetClass != null
                    && !Object.class.equals(targetClass)
                    && !targetClass.getName().startsWith("java.")) {
                for (Field field : targetClass.getDeclaredFields()) {
                    if (field.isSynthetic()
                            || Modifier.isStatic(field.getModifiers())
                            || Modifier.isFinal(field.getModifiers())) {
                        continue;
                    }
                    field.setAccessible(true);
                    fields.add(field);
                }
                targetClass = targetClass.getSuperclass();
            }
            return fields;
        });
    }

    private boolean isSimpleValueType(Class<?> valueClass) {
        return valueClass.isPrimitive()
                || ClassUtils.isPrimitiveOrWrapper(valueClass)
                || CharSequence.class.isAssignableFrom(valueClass)
                || Number.class.isAssignableFrom(valueClass)
                || Date.class.isAssignableFrom(valueClass)
                || Temporal.class.isAssignableFrom(valueClass)
                || UUID.class.isAssignableFrom(valueClass)
                || valueClass.isEnum()
                || MultipartFile.class.isAssignableFrom(valueClass)
                || Controller.class.isAssignableFrom(valueClass);
    }

    @Nullable
    private HttpServletRequest currentRequest() {
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return requestAttributes == null ? null : requestAttributes.getRequest();
    }
}
