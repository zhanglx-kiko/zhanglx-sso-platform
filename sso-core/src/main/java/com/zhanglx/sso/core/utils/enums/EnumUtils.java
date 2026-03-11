package com.zhanglx.sso.core.utils.enums;

import com.zhanglx.sso.core.enums.IBaseEnum;
import org.apache.commons.lang3.Strings;

import java.util.Objects;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/5 16:49
 * @ClassName: EnumUtils
 * @Description:
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
