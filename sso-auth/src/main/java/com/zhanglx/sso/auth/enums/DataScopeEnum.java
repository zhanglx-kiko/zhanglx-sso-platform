package com.zhanglx.sso.auth.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.zhanglx.sso.core.enums.IBaseEnum;
import com.zhanglx.sso.core.enums.IIntegerBaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@Getter
@RequiredArgsConstructor
public enum DataScopeEnum implements IIntegerBaseEnum<String> {

    ALL(1, "全部"),
    DEPT_AND_CHILDREN(2, "本部门及以下"),
    DEPT(3, "本部门"),
    SELF(4, "本人"),
    CUSTOM(5, "自定义");

    @JsonValue
    private final Integer code;

    private final String description;

    public static DataScopeEnum fromCode(Integer code) {
        return IBaseEnum.fromCode(code, DataScopeEnum.class);
    }

    public boolean matches(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof DataScopeEnum dataScopeEnum) {
            return this == dataScopeEnum;
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