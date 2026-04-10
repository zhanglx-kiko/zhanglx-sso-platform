package com.zhanglx.sso.common.net;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 客户端地址解析工具。
 * 用于在可信代理链场景下还原真实来源地址。
 */
public final class ClientIpUtils {

    /**
     * 工具类不允许实例化。
     */
    private ClientIpUtils() {
    }

    public static String resolveClientIp(String remoteAddress,
                                         String xForwardedFor,
                                         String xRealIp,
                                         Collection<String> trustedProxyCidrs) {
        String normalizedRemoteAddress = normalizeIp(remoteAddress);
        if (!hasText(normalizedRemoteAddress)) {
            return firstNonBlank(normalizeIp(xRealIp), firstForwardedIp(xForwardedFor));
        }

        List<CidrMatcher> matchers = parseTrustedMatchers(trustedProxyCidrs);
        if (!isTrustedProxy(normalizedRemoteAddress, matchers)) {
            return normalizedRemoteAddress;
        }

        List<String> forwardedChain = parseForwardedChain(xForwardedFor);
        if (!forwardedChain.isEmpty()) {
            for (int i = forwardedChain.size() - 1; i >= 0; i--) {
                String candidate = forwardedChain.get(i);
                if (!isTrustedProxy(candidate, matchers)) {
                    return candidate;
                }
            }
            return forwardedChain.getFirst();
        }

        String normalizedRealIp = normalizeIp(xRealIp);
        if (hasText(normalizedRealIp) && !isTrustedProxy(normalizedRealIp, matchers)) {
            return normalizedRealIp;
        }

        return normalizedRemoteAddress;
    }

    public static boolean isTrustedProxy(String ipAddress, Collection<String> trustedProxyCidrs) {
        return isTrustedProxy(normalizeIp(ipAddress), parseTrustedMatchers(trustedProxyCidrs));
    }

    static boolean isTrustedProxy(String ipAddress, List<CidrMatcher> matchers) {
        if (!hasText(ipAddress) || isEmpty(matchers)) {
            return false;
        }

        return matchers.stream().anyMatch(matcher -> matcher.matches(ipAddress));
    }

    static List<CidrMatcher> parseTrustedMatchers(Collection<String> trustedProxyCidrs) {
        if (isEmpty(trustedProxyCidrs)) {
            return List.of();
        }

        return trustedProxyCidrs.stream()
                .filter(ClientIpUtils::hasText)
                .map(String::trim)
                .distinct()
                .map(CidrMatcher::new)
                .toList();
    }

    public static String normalizeIp(String rawAddress) {
        if (!hasText(rawAddress)) {
            return null;
        }

        String normalized = rawAddress.trim();
        if ("unknown".equalsIgnoreCase(normalized)) {
            return null;
        }

        if (normalized.startsWith("[")) {
            int closingBracket = normalized.indexOf(']');
            if (closingBracket > 0) {
                return normalized.substring(1, closingBracket);
            }
        }

        long colonCount = normalized.chars().filter(ch -> ch == ':').count();
        if (colonCount == 1 && normalized.indexOf('.') > 0) {
            int separatorIndex = normalized.lastIndexOf(':');
            return normalized.substring(0, separatorIndex);
        }

        return normalized;
    }

    static List<String> parseForwardedChain(String forwardedFor) {
        if (!hasText(forwardedFor)) {
            return List.of();
        }

        String[] chain = forwardedFor.split(",");
        List<String> result = new ArrayList<>(chain.length);
        for (String segment : chain) {
            String normalized = normalizeIp(segment);
            if (hasText(normalized)) {
                result.add(normalized);
            }
        }

        return result;
    }

    /**
     * 提取转发链中的首个地址。
     */
    private static String firstForwardedIp(String forwardedFor) {
        List<String> chain = parseForwardedChain(forwardedFor);
        return chain.isEmpty() ? null : chain.getFirst();
    }

    /**
     * 返回两个参数中的第一个非空白值。
     */
    private static String firstNonBlank(String first, String second) {
        if (hasText(first)) {
            return first;
        }

        return hasText(second) ? second : null;
    }

    /**
     * 判断字符串是否包含有效文本。
     */
    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * 判断集合是否为空。
     */
    private static boolean isEmpty(Collection<?> values) {
        return values == null || values.isEmpty();
    }
}
