package com.zhanglx.sso.xss.filter;

import com.zhanglx.sso.xss.support.XssAuditRecorder;
import com.zhanglx.sso.xss.support.XssSanitizationService;
import com.zhanglx.sso.xss.wrapper.XssHttpServletRequestWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * XSS 全局过滤器。
 * 这里优先覆盖 Query、Form 和指定请求头，并在请求结束时统一刷审计指标。
 */
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class XssProtectionFilter extends OncePerRequestFilter {

    private final XssSanitizationService sanitizationService;
    /**
     * 审计记录器。
     */
    private final XssAuditRecorder auditRecorder;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest activeRequest = request;
        try {
            if (!sanitizationService.shouldSkipRequest(request)) {
                activeRequest = new XssHttpServletRequestWrapper(request, sanitizationService);
            }
            filterChain.doFilter(activeRequest, response);
        } finally {
            auditRecorder.flush(request);
        }
    }
}