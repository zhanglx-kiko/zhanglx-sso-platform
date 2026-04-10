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
public enum PermissionTypeEnum implements IIntegerBaseEnum<String> {

    PLATFORM(-1, "平台"),
    MODULE(0, "模块"),
    MENU(1, "菜单"),
    BUTTON(2, "按钮"),
    API(3, "接口");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String description;

    public static PermissionTypeEnum fromCode(Integer code) {
        return IBaseEnum.fromCode(code, PermissionTypeEnum.class);
    }

    public boolean matches(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof PermissionTypeEnum permissionTypeEnum) {
            return this == permissionTypeEnum;
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
