package com.zhanglx.sso.auth.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.zhanglx.sso.core.enums.IBaseEnum;
import com.zhanglx.sso.core.enums.IIntegerBaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * 用户状态枚举。
 */
@Getter
@RequiredArgsConstructor
public enum UserStatusEnum implements IIntegerBaseEnum<String> {

    DISABLED(0, "禁用"),
    NORMAL(1, "正常"),
    FROZEN(2, "冻结"),
    CANCELLING(3, "注销中"),
    CANCELLED(4, "已注销");

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

    public static UserStatusEnum fromCode(Integer code) {
        return IBaseEnum.fromCode(code, UserStatusEnum.class);
    }

    public static UserStatusEnum normalize(UserStatusEnum status) {
        return status == null ? NORMAL : status;
    }

    public boolean isNormal() {
        return this == NORMAL;
    }

    public boolean isDisabled() {
        return this == DISABLED;
    }

    public boolean isFrozen() {
        return this == FROZEN;
    }

    public boolean isCancelling() {
        return this == CANCELLING;
    }

    public boolean isCancelled() {
        return this == CANCELLED;
    }

    /**
     * 当前状态是否允许继续登录。
     */
    public boolean canLogin() {
        return this == NORMAL;
    }

    public boolean matches(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof UserStatusEnum statusEnum) {
            return this == statusEnum;
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