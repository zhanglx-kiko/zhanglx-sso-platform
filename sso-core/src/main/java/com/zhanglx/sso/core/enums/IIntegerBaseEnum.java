package com.zhanglx.sso.core.enums;

/**
 * 整型基础枚举接口。
 */
public interface IIntegerBaseEnum<V> extends IBaseEnum<Integer, V> {

    @Override
    Integer getCode();

}