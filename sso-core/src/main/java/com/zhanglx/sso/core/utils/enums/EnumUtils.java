package com.zhanglx.sso.core.utils.enums;

import com.zhanglx.sso.core.enums.IBaseEnum;
import org.apache.commons.lang3.Strings;

import java.util.Objects;

/**
 * 作者：Zhang L X
 * 创建时间：2026/3/5 16:49
 * 类名：EnumUtils
 * 说明：
 */
public class EnumUtils {
    public static <T extends IBaseEnum> T codeOf(Class<T> enumClass, Object code) {
        T[] enumConstants = enumClass.getEnumConstants();
        if (Objects.isNull(enumConstants)) {
            return null;
        }

        for (T t : enumConstants) {
            if (Strings.CS.equals(t.getCode().toString(), code.toString())) {
                return t;
            }
        }

        return null;
    }

}