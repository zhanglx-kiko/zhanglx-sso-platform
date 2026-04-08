package com.zhanglx.sso.web.support;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;

public interface RequestActorResolver extends Ordered {

    default String resolveActor(HttpServletRequest request) {
        return null;
    }

    default String resolveUserId(HttpServletRequest request) {
        return null;
    }

    default String resolveToken(HttpServletRequest request) {
        return null;
    }

    default String resolveTenantId(HttpServletRequest request) {
        return null;
    }

    @Override
    default int getOrder() {
        return 0;
    }
}
