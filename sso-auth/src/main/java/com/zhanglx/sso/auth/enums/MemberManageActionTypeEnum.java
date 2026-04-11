package com.zhanglx.sso.auth.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.zhanglx.sso.core.enums.IBaseEnum;
import com.zhanglx.sso.core.enums.IIntegerBaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * 会员后台管理动作类型。
 */
@Getter
@RequiredArgsConstructor
public enum MemberManageActionTypeEnum implements IIntegerBaseEnum<String> {

    ENABLE(1, "启用"),
    DISABLE(2, "禁用"),
    FREEZE(3, "冻结"),
    UNFREEZE(4, "解冻"),
    FORCE_LOGOUT(5, "强制下线"),
    CANCEL(6, "注销"),
    RECOVER(7, "恢复"),
    ADD_BLACKLIST(8, "加入黑名单"),
    REMOVE_BLACKLIST(9, "移除黑名单");

    /**
     * 枚举编码。
     */
    @EnumValue
    @JsonValue
    private final Integer code;
    /**
     * 枚举说明。
     */
    private final String description;

    public static MemberManageActionTypeEnum fromCode(Integer code) {
        return IBaseEnum.fromCode(code, MemberManageActionTypeEnum.class);
    }

    public boolean matches(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof MemberManageActionTypeEnum actionTypeEnum) {
            return this == actionTypeEnum;
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