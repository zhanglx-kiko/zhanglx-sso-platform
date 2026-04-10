package com.zhanglx.sso.auth.config;

import cn.dev33.satoken.stp.StpUtil;
import com.zhanglx.sso.core.utils.satoken.StpMemberUtil;
import com.zhanglx.sso.web.support.RequestActorResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 认证请求操作人解析器。
 */
@Component
public class AuthRequestActorResolver implements RequestActorResolver {

    @Override
    public String resolveActor(HttpServletRequest request) {
        String userId = resolveUserId(request);
        return StringUtils.hasText(userId) ? userId : null;
    }

    @Override
    public String resolveUserId(HttpServletRequest request) {
        if (StpUtil.isLogin()) {
            return "sys:" + StpUtil.getLoginIdAsString();
        }

        if (StpMemberUtil.isLogin()) {
            return "member:" + StpMemberUtil.getLoginIdAsLong();
        }

        return null;
    }

    @Override
    public String resolveToken(HttpServletRequest request) {
        if (StpUtil.isLogin()) {
            return "sys:" + StpUtil.getTokenValue();
        }

        if (StpMemberUtil.isLogin()) {
            return "member:" + StpMemberUtil.getTokenValue();
        }

        return null;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}