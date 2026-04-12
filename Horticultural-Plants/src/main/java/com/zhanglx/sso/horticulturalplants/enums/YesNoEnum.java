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
public enum YesNoEnum implements IIntegerBaseEnum<String> {

    NO(0, "否"),
    YES(1, "是");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String description;

    public static YesNoEnum fromCode(Integer code) {
        return IBaseEnum.fromCode(code, YesNoEnum.class);
    }

    public boolean matches(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof YesNoEnum yesNoEnum) {
            return this == yesNoEnum;
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
