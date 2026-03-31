package com.zhanglx.sso.core.utils;

import cn.hutool.core.util.StrUtil;
import com.zhanglx.sso.core.exception.BusinessException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/18 18:17
 * @ClassName: AssertUtils
 * @Description: 业务断言工具类
 */
public class AssertUtils {

    // 私有化构造器，防止实例化
    private AssertUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 基础抛出异常方法
     */
    private static void throwException(String message) {
        throw new BusinessException(message);
    }

    private static void throwException(Supplier<String> messageSupplier) {
        throw new BusinessException(messageSupplier != null ? messageSupplier.get() : "业务校验失败");
    }

    // ==========================================
    // 布尔值断言
    // ==========================================

    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throwException(message);
        }
    }

    public static void isTrue(boolean expression, Supplier<String> messageSupplier) {
        if (!expression) {
            throwException(messageSupplier);
        }
    }

    public static void isFalse(boolean expression, String message) {
        if (expression) {
            throwException(message);
        }
    }

    // ==========================================
    // Null 值断言
    // ==========================================

    public static void notNull(Object object, String message) {
        if (object == null) {
            throwException(message);
        }
    }

    public static void notNull(Object object, Supplier<String> messageSupplier) {
        if (object == null) {
            throwException(messageSupplier);
        }
    }

    public static void isNull(Object object, String message) {
        if (object != null) {
            throwException(message);
        }
    }

    // ==========================================
    // 字符串断言
    // ==========================================

    public static void notBlank(String text, String message) {
        if (StrUtil.isBlank(text)) {
            throwException(message);
        }
    }

    public static void notBlank(String text, Supplier<String> messageSupplier) {
        if (StrUtil.isBlank(text)) {
            throwException(messageSupplier);
        }
    }

    // ==========================================
    // 集合与数组断言
    // ==========================================

    public static void notEmpty(Collection<?> collection, String message) {
        if (CollectionUtils.isEmpty(collection)) {
            throwException(message);
        }
    }

    public static void notEmpty(Map<?, ?> map, String message) {
        if (CollectionUtils.isEmpty(map)) {
            throwException(message);
        }
    }

    public static void notEmpty(Object[] array, String message) {
        if (ObjectUtils.isEmpty(array)) {
            throwException(message);
        }
    }

}
