package com.zhanglx.sso.core.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/5 16:22
 * @ClassName: IBaseEnum
 * @Description:
 */
public interface IBaseEnum<K, V> {
    static <E extends Enum<E>> IBaseEnum valueOf(String enumCode, Class<E> clazz) {
        return (IBaseEnum) Enum.valueOf(clazz, enumCode);
    }

    static <K, E extends Enum<E> & IBaseEnum<K, ?>> E fromCode(K code, Class<E> clazz) {
        if (code == null || clazz == null) {
            return null;
        }
        return Arrays.stream(clazz.getEnumConstants())
                .filter(item -> Objects.equals(item.getCode(), code))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取code
     *
     * @return
     */
    K getCode();

    /**
     * 获取描述
     *
     * @return
     */
    V getDescription();

    /**
     * @return code 值列表
     */
    @SuppressWarnings("unchecked")
    default List<K> codeValues() {
        Enum<?> current = (Enum<?>) this;
        return Arrays.stream(current.getDeclaringClass().getEnumConstants())
                .map(item -> ((IBaseEnum<K, V>) item).getCode())
                .toList();
    }

}
