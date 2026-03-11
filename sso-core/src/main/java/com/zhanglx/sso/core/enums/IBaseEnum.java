package com.zhanglx.sso.core.enums;

import java.util.List;

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
    List<K> codeValues();

}
