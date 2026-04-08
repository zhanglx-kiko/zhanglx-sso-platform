package com.zhanglx.sso.auth.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.zhanglx.sso.core.enums.IBaseEnum;
import com.zhanglx.sso.core.enums.IIntegerBaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

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

    public boolean matches(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof ConfigTypeEnum configTypeEnum) {
            return this == configTypeEnum;
        }
        if (value instanceof Number number) {
            return Objects.equals(code, number.intValue());
        }
        if (value instanceof String text) {
            return Objects.equals(String.valueOf(code), text) || name().equalsIgnoreCase(text);
        }
        return false;
    }

    public static boolean isBuiltIn(Object value) {
        return BUILT_IN.matches(value);
    }
}