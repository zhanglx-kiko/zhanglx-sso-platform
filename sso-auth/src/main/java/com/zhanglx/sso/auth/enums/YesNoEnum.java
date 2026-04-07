package com.zhanglx.sso.auth.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.zhanglx.sso.core.enums.IBaseEnum;
import com.zhanglx.sso.core.enums.IIntegerBaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum YesNoEnum implements IIntegerBaseEnum<String> {

    NO(0, "否"),
    YES(1, "是");

    @JsonValue
    private final Integer code;

    private final String description;

    public static YesNoEnum fromCode(Integer code) {
        return IBaseEnum.fromCode(code, YesNoEnum.class);
    }

    public boolean matches(Number value) {
        return value != null && value.intValue() == code;
    }

    public Short toShortCode() {
        return code.shortValue();
    }

}
