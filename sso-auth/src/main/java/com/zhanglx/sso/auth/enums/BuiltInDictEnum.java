package com.zhanglx.sso.auth.enums;

import com.zhanglx.sso.core.enums.IStringBaseEnum;

/**
 * 内置字典枚举接口。
 */
public interface BuiltInDictEnum extends IStringBaseEnum<String> {

    SystemDictTypeEnum getDictType();

}