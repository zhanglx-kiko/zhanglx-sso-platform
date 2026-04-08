package com.zhanglx.sso.core.trace;

import com.zhanglx.sso.common.trace.TraceConstants;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

/**
 * 统一封装 traceId / requestId 的读写。
 * 这里直接复用 MDC，避免再维护一套额外 ThreadLocal。
 */
public final class TraceContextHolder {

    private TraceContextHolder() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void setTraceId(String traceId) {
        put(TraceConstants.MDC_TRACE_ID, traceId);
    }

    public static void setRequestId(String requestId) {
        put(TraceConstants.MDC_REQUEST_ID, requestId);
    }

    public static String getTraceId() {
        return trimToNull(MDC.get(TraceConstants.MDC_TRACE_ID));
    }

    public static String getRequestId() {
        return trimToNull(MDC.get(TraceConstants.MDC_REQUEST_ID));
    }

    public static void clear() {
        MDC.remove(TraceConstants.MDC_TRACE_ID);
        MDC.remove(TraceConstants.MDC_REQUEST_ID);
    }

    private static void put(String key, String value) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            MDC.remove(key);
            return;
        }
        MDC.put(key, normalized);
    }

    private static String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
