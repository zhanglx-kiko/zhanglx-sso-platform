package com.zhanglx.sso.auth.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.zhanglx.sso.core.enums.IBaseEnum;
import com.zhanglx.sso.core.enums.IIntegerBaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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

    public boolean matches(Number value) {
        return value != null && value.intValue() == code;
    }

    public Short toShortCode() {
        return code.shortValue();
    }

    public static boolean isEnabled(Number value) {
        return ENABLED.matches(value);
    }

    public static boolean isDisabled(Number value) {
        return DISABLED.matches(value);
    }

}
