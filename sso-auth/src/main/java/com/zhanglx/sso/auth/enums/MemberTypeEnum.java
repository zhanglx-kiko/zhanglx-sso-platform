package com.zhanglx.sso.auth.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.zhanglx.sso.core.enums.IBaseEnum;
import com.zhanglx.sso.core.enums.IIntegerBaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * 会员类型枚举。
 */
@Getter
@RequiredArgsConstructor
public enum MemberTypeEnum implements IIntegerBaseEnum<String> {

    NORMAL(0, "普通会员"),
    VIP(1, "VIP会员");

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

    public static MemberTypeEnum fromCode(Integer code) {
        return IBaseEnum.fromCode(code, MemberTypeEnum.class);
    }

    public boolean matches(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof MemberTypeEnum memberTypeEnum) {
            return this == memberTypeEnum;
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