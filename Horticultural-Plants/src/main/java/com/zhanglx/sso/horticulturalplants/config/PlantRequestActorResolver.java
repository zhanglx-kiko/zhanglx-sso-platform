package com.zhanglx.sso.horticulturalplants.config;

import cn.dev33.satoken.stp.StpUtil;
import com.zhanglx.sso.core.utils.satoken.StpMemberUtil;
import com.zhanglx.sso.web.support.RequestActorResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class PlantRequestActorResolver implements RequestActorResolver {

    @Override
    public String resolveActor(HttpServletRequest request) {
        String userId = resolveUserId(request);
        return StringUtils.hasText(userId) ? userId : null;
    }

    @Override
    public String resolveUserId(HttpServletRequest request) {
        return resolveQuietly(() -> {
            if (StpUtil.isLogin()) {
                return "sys:" + StpUtil.getLoginIdAsString();
            }
            if (StpMemberUtil.isLogin()) {
                return "member:" + StpMemberUtil.getLoginIdAsLong();
            }
            return null;
        });
    }

    @Override
    public String resolveToken(HttpServletRequest request) {
        return resolveQuietly(() -> {
            if (StpUtil.isLogin()) {
                return "sys:" + StpUtil.getTokenValue();
            }
            if (StpMemberUtil.isLogin()) {
                return "member:" + StpMemberUtil.getTokenValue();
            }
            return null;
        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private String resolveQuietly(ResolverAction action) {
        try {
            return action.resolve();
        } catch (Exception e) {
            log.debug("请求身份解析时忽略无效登录态: {}", e.getMessage(), e);
            return null;
        }
    }

    @FunctionalInterface
    private interface ResolverAction {
        String resolve();
    }
}
