package com.zhanglx.sso.xss.support;

/**
 * XSS 策略解析结果。
 *
 * @param mode 最终生效策略
 * @param bypassReason 如果命中放行，记录对应原因
 */
public record XssPolicyResolution(XssPolicyMode mode, XssBypassReason bypassReason) {

    public static XssPolicyResolution of(XssPolicyMode mode) {
        return new XssPolicyResolution(mode, null);
    }

    public static XssPolicyResolution bypass(XssBypassReason bypassReason) {
        return new XssPolicyResolution(XssPolicyMode.NONE, bypassReason);
    }
}
