package com.zhanglx.sso.auth.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.zhanglx.sso.core.enums.IBaseEnum;
import com.zhanglx.sso.core.enums.IIntegerBaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * 会员实名状态枚举。
 */
@Getter
@RequiredArgsConstructor
public enum RealNameStatusEnum implements IIntegerBaseEnum<String> {

    UNVERIFIED(0, "未认证"),
    VERIFYING(1, "认证中"),
    VERIFIED(2, "已认证"),
    FAILED(3, "认证失败");

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

    public static RealNameStatusEnum fromCode(Integer code) {
        return IBaseEnum.fromCode(code, RealNameStatusEnum.class);
    }

    public boolean matches(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof RealNameStatusEnum realNameStatusEnum) {
            return this == realNameStatusEnum;
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