package com.zhanglx.sso.common.net;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * 网段匹配器。
 * 用于校验某个地址是否落在指定的 CIDR 网段范围内。
 */
public final class CidrMatcher {
    /**
     * 网络地址字节数组。
     */
    private final byte[] networkAddress;

    /**
     * 前缀长度。
     */
    private final int prefixLength;

    public CidrMatcher(String cidr) {
        if (cidr == null || cidr.isBlank()) {
            throw new IllegalArgumentException("cidr cannot be blank");
        }

        String normalized = cidr.trim();
        String addressPart = normalized;
        int calculatedPrefixLength;
        int separatorIndex = normalized.indexOf('/');
        if (separatorIndex >= 0) {
            addressPart = normalized.substring(0, separatorIndex).trim();
            calculatedPrefixLength = Integer.parseInt(normalized.substring(separatorIndex + 1).trim());
        } else {
            calculatedPrefixLength = toAddress(addressPart).length * Byte.SIZE;
        }

        byte[] addressBytes = toAddress(addressPart);
        int maxPrefixLength = addressBytes.length * Byte.SIZE;
        if (calculatedPrefixLength < 0 || calculatedPrefixLength > maxPrefixLength) {
            throw new IllegalArgumentException("invalid prefix length: " + normalized);
        }

        this.networkAddress = addressBytes;
        this.prefixLength = calculatedPrefixLength;
    }

    /**
     * 将地址字符串解析为字节数组。
     */
    private static byte[] toAddress(String rawAddress) {
        try {
            return InetAddress.getByName(rawAddress).getAddress();
        } catch (UnknownHostException ex) {
            throw new IllegalArgumentException("invalid ip/cidr address: " + rawAddress, ex);
        }
    }

    public boolean matches(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return false;
        }

        byte[] candidate = toAddress(ipAddress.trim());
        if (candidate.length != networkAddress.length) {
            return false;
        }

        int fullBytes = prefixLength / Byte.SIZE;
        int remainingBits = prefixLength % Byte.SIZE;
        if (fullBytes > 0 && !Arrays.equals(
                Arrays.copyOf(candidate, fullBytes),
                Arrays.copyOf(networkAddress, fullBytes))) {
            return false;
        }

        if (remainingBits == 0) {
            return true;
        }

        int mask = (-1) << (Byte.SIZE - remainingBits);
        return (candidate[fullBytes] & mask) == (networkAddress[fullBytes] & mask);
    }
}
