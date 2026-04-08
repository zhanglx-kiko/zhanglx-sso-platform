package com.zhanglx.sso.auth.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.zhanglx.sso.core.enums.IBaseEnum;
import com.zhanglx.sso.core.enums.IIntegerBaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@Getter
@RequiredArgsConstructor
public enum UserStatusEnum implements IIntegerBaseEnum<String> {

    DISABLED(0, "禁用"),
    NORMAL(1, "正常");

    @JsonValue
    private final Integer code;

    private final String description;

    public static UserStatusEnum fromCode(Integer code) {
        return IBaseEnum.fromCode(code, UserStatusEnum.class);
    }

    public boolean matches(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof UserStatusEnum statusEnum) {
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