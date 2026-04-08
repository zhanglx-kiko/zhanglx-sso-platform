package com.zhanglx.sso.web.support;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RateLimitDecision {

    private final boolean allowed;

    private final long limit;

    private final long remaining;

    private final long resetSeconds;

    private final long current;
}
