package com.zhanglx.sso.auth.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.zhanglx.sso.core.enums.IBaseEnum;
import com.zhanglx.sso.core.enums.IIntegerBaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PermissionTypeEnum implements IIntegerBaseEnum<String> {

    PLATFORM((short) -1, "平台"),
    MODULE((short) 0, "模块"),
    MENU((short) 1, "菜单"),
    BUTTON((short) 2, "按钮"),
    API((short) 3, "接口");

    @JsonValue
    private final Short code;

    private final String description;

    public static PermissionTypeEnum fromCode(Integer code) {
        return IBaseEnum.fromCode(code, PermissionTypeEnum.class);
    }

    public boolean matches(Number value) {
        return value != null && value.shortValue() == code;
    }

}
