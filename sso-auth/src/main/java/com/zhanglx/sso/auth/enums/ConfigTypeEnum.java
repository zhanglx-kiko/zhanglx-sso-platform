package com.zhanglx.sso.auth.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.zhanglx.sso.core.enums.IBaseEnum;
import com.zhanglx.sso.core.enums.IIntegerBaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConfigTypeEnum implements IIntegerBaseEnum<String> {

    CUSTOM(0, "普通参数"),
    BUILT_IN(1, "系统内置");

    @JsonValue
    private final Integer code;

    private final String description;

    public static ConfigTypeEnum fromCode(Integer code) {
        return IBaseEnum.fromCode(code, ConfigTypeEnum.class);
    }

    public boolean matches(Number value) {
        return value != null && value.intValue() == code;
    }

    public static boolean isBuiltIn(Number value) {
        return BUILT_IN.matches(value);
    }

}
