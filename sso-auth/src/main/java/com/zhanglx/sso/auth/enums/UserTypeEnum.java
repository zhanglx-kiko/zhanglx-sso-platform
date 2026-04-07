package com.zhanglx.sso.auth.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.zhanglx.sso.core.enums.IBaseEnum;
import com.zhanglx.sso.core.enums.IIntegerBaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserTypeEnum implements IIntegerBaseEnum<String> {

    SYSTEM(1, "系统用户"),
    MEMBER(2, "会员用户");

    @JsonValue
    private final Integer code;

    private final String description;

    public static UserTypeEnum fromCode(Integer code) {
        return IBaseEnum.fromCode(code, UserTypeEnum.class);
    }

    public boolean matches(Number value) {
        return value != null && value.intValue() == code;
    }

}
