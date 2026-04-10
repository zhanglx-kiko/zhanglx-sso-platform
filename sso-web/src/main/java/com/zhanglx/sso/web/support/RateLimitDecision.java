package com.zhanglx.sso.web.support;

/**
 * RateLimitDecision类型。
 *
 * @param allowed      allowed。
 * @param limit        限制。
 * @param remaining    remaining。
 * @param resetSeconds resetSeconds。
 * @param current      当前值。
 */
public record RateLimitDecision(boolean allowed, long limit, long remaining, long resetSeconds, long current) {
}