package com.zhanglx.sso.auth.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.zhanglx.sso.core.enums.IBaseEnum;
import com.zhanglx.sso.core.enums.IIntegerBaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PermissionTypeEnum implements IIntegerBaseEnum<String> {

    PLATFORM(-1, "平台"),
    MODULE(0, "模块"),
    MENU(1, "菜单"),
    BUTTON(2, "按钮"),
    API(3, "接口");

    @JsonValue
    private final Integer code;

    private final String description;

    public static PermissionTypeEnum fromCode(Integer code) {
        return IBaseEnum.fromCode(code, PermissionTypeEnum.class);
    }

    public boolean matches(Number value) {
        return value != null && value.equals(code);
    }

}
