package com.zhanglx.sso.horticulturalplants.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
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

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String description;

    public static EnableStatusEnum fromCode(Integer code) {
        return IBaseEnum.fromCode(code, EnableStatusEnum.class);
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
