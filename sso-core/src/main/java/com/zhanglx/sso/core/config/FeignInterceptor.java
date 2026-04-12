package com.zhanglx.sso.core.config;

import cn.dev33.satoken.same.SaSameUtil;
import cn.dev33.satoken.stp.StpUtil;
import com.zhanglx.sso.common.trace.TraceConstants;
import com.zhanglx.sso.core.trace.TraceContextHolder;
import com.zhanglx.sso.core.utils.satoken.StpMemberUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

/**
 * 作者：Zhang L X
 * 创建时间：2026/3/24 15:37
 * 类名：FeignInterceptor
 * 说明：Feign 全局请求拦截器，解决内部 RPC 调用的鉴权问题
 * 网关只负责在入口处塞入 同源令牌。微服务之间内部调用时，默认是不经过网关的，会导致 Feign 发出的请求没有 同源令牌，被拦截器拒绝访问。
 */
@Configuration
public class FeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        // 1. 传递 同源令牌（防绕过网关），这个在任何线程都能生成
        template.header(SaSameUtil.SAME_TOKEN, SaSameUtil.getToken());

        String traceId = TraceContextHolder.getTraceId();
        if (traceId != null) {
            template.header(TraceConstants.TRACE_ID_HEADER, traceId);
        }

        String requestId = TraceContextHolder.getRequestId();
        if (requestId != null) {
            template.header(TraceConstants.REQUEST_ID_HEADER, requestId);
        }

        // 2. 传递 用户令牌（用户身份验证）
        // 先判断当前是否在正常的 Web 上下文中，并且已经登录
        try {
            if (StpUtil.isLogin()) {
                template.header(StpUtil.getTokenName(), StpUtil.getTokenValue());
            } else if (StpMemberUtil.isLogin()) {
                template.header(StpMemberUtil.getStpLogic().getTokenName(), StpMemberUtil.getTokenValue());
            }
        } catch (Exception e) {
            // 在极少数非 Web 环境且未配置上下文透传的情况下，忽略异常，避免阻断内部定时任务等逻辑
        }
    }

}
