package com.zhanglx.sso.core.config;

import cn.dev33.satoken.same.SaSameUtil;
import cn.dev33.satoken.stp.StpUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
/**
 * @Author: Zhang L X
 * @Create: 2026/3/24 15:37
 * @ClassName: FeignInterceptor
 * @Description: Feign 全局请求拦截器，解决内部 RPC 调用的鉴权问题
 * 网关只负责在入口处塞入 Same-Token。微服务之间内部调用时，默认是不经过网关的，会导致 Feign 发出的请求没有 Same-Token，被拦截器拒绝访问。
 */
@Configuration
public class FeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        // 1. 传递 Same-Token（防绕过网关），这个在任何线程都能生成
        template.header(SaSameUtil.SAME_TOKEN, SaSameUtil.getToken());

        // 2. 传递 User-Token（用户身份验证）
        // 先判断当前是否在正常的 Web 上下文中，并且已经登录
        try {
            if (StpUtil.isLogin()) {
                template.header(StpUtil.getTokenName(), StpUtil.getTokenValue());
            }
        } catch (Exception e) {
            // 在极少数非 Web 环境且未配置上下文透传的情况下，忽略异常，避免阻断内部定时任务等逻辑
        }
    }

}
