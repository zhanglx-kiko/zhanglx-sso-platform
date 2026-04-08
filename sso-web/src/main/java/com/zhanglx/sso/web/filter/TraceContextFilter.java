package com.zhanglx.sso.web.filter;

import com.zhanglx.sso.common.trace.TraceConstants;
import com.zhanglx.sso.core.trace.TraceContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 统一在服务入口处补齐 traceId / requestId，并写入 MDC。
 * 这样无论是业务日志、登录日志还是 ES 操作日志，都能拿到同一组标识。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class TraceContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = firstNonBlank(request.getHeader(TraceConstants.REQUEST_ID_HEADER), UUID.randomUUID().toString());
        String traceId = firstNonBlank(request.getHeader(TraceConstants.TRACE_ID_HEADER), requestId);

        request.setAttribute(TraceConstants.REQUEST_ID_ATTRIBUTE, requestId);
        request.setAttribute(TraceConstants.TRACE_ID_ATTRIBUTE, traceId);
        response.setHeader(TraceConstants.REQUEST_ID_HEADER, requestId);
        response.setHeader(TraceConstants.TRACE_ID_HEADER, traceId);

        TraceContextHolder.setRequestId(requestId);
        TraceContextHolder.setTraceId(traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            TraceContextHolder.clear();
        }
    }

    private String firstNonBlank(String first, String second) {
        if (StringUtils.hasText(first)) {
            return first.trim();
        }
        return StringUtils.hasText(second) ? second.trim() : null;
    }
}
