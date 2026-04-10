package com.zhanglx.sso.auth.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.zhanglx.sso.core.enums.IBaseEnum;
import com.zhanglx.sso.core.enums.IIntegerBaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@Getter
@RequiredArgsConstructor
public enum UserTypeEnum implements IIntegerBaseEnum<String> {

    SYSTEM(1, "系统用户"),
    MEMBER(2, "会员用户");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String description;

    public static UserTypeEnum fromCode(Integer code) {
        return IBaseEnum.fromCode(code, UserTypeEnum.class);
    }

    public boolean matches(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof UserTypeEnum userTypeEnum) {
            return this == userTypeEnum;
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
