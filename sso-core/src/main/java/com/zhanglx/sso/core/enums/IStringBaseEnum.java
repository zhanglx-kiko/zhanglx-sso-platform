package com.zhanglx.sso.core.enums;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/5 16:26
 * @ClassName: IStringBaseEnum
 * @Description:
 */
public interface IStringBaseEnum<V> extends IBaseEnum<String, V> {

    @Override
    String getCode();

}
