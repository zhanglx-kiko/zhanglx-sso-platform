package com.zhanglx.sso.auth.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.zhanglx.sso.core.enums.IBaseEnum;
import com.zhanglx.sso.core.enums.IIntegerBaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@Getter
@RequiredArgsConstructor
public enum EnableStatusEnum implements IIntegerBaseEnum<String> {

    DISABLED(0, "停用"),
    ENABLED(1, "启用");

    @JsonValue
    private final Integer code;

    private final String description;

    public static EnableStatusEnum fromCode(Integer code) {
        return IBaseEnum.fromCode(code, EnableStatusEnum.class);
    }

    public static boolean isEnabled(Object value) {
        return ENABLED.matches(value);
    }

    public static boolean isDisabled(Object value) {
        return DISABLED.matches(value);
    }

    public boolean matches(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof EnableStatusEnum statusEnum) {
            return this == statusEnum;
        }
        if (value instanceof Number number) {
            return Objects.equals(code, number.intValue());
        }
        if (value instanceof String text) {
            return Objects.equals(String.valueOf(code), text) || name().equalsIgnoreCase(text);
        }
        return false;
    }
}