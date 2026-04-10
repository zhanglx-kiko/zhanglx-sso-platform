package com.zhanglx.sso.auth.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.zhanglx.sso.core.enums.IBaseEnum;
import com.zhanglx.sso.core.enums.IIntegerBaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 性别枚举枚举。
 */
@Getter
@RequiredArgsConstructor
public enum GenderEnum implements IIntegerBaseEnum<String> {

    UNKNOWN(0, "未知"),
    MALE(1, "男"),
    FEMALE(2, "女");

    /**
     * 验证码。
     */
    @EnumValue
    @JsonValue
    private final Integer code;
    /**
     * 说明。
     */
    private final String description;

    public static GenderEnum fromCode(Integer code) {
        return IBaseEnum.fromCode(code, GenderEnum.class);
    }

}