package com.zhanglx.sso.core.enums;

/**
 * 作者：Zhang L X
 * 创建时间：2026/3/5 16:26
 * 类名：字符串基础枚举接口
 * 说明：
 */
public interface IStringBaseEnum<V> extends IBaseEnum<String, V> {

    @Override
    String getCode();

}