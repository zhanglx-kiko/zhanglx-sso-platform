package com.zhanglx.sso.common.trace;

/**
 * Trace / Request 标识常量。
 * 这里只放纯常量，避免网关为了引用请求头名称而额外依赖 sso-core。
 */
public final class TraceConstants {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    public static final String TRACE_ID_ATTRIBUTE = TraceConstants.class.getName() + ".traceId";
    public static final String REQUEST_ID_ATTRIBUTE = TraceConstants.class.getName() + ".requestId";

    public static final String MDC_TRACE_ID = "traceId";
    public static final String MDC_REQUEST_ID = "requestId";

    private TraceConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}
