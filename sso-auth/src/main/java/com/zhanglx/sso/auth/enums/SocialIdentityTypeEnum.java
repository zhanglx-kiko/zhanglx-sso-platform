package com.zhanglx.sso.auth.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.zhanglx.sso.core.enums.IBaseEnum;
import com.zhanglx.sso.core.enums.IStringBaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@Getter
@RequiredArgsConstructor
public enum SocialIdentityTypeEnum implements IStringBaseEnum<String> {

    WECHAT_OPEN("WECHAT_OPEN", "微信开放平台"),
    WX_MINI("WX_MINI", "微信小程序");

    @EnumValue
    @JsonValue
    private final String code;

    private final String description;

    public static SocialIdentityTypeEnum fromCode(String code) {
        return IBaseEnum.fromCode(code, SocialIdentityTypeEnum.class);
    }

    public boolean matches(Object value) {
        if (value == null) {
            return false;
        }

        if (value instanceof SocialIdentityTypeEnum identityTypeEnum) {
            return this == identityTypeEnum;
        }

        return Objects.equals(code, String.valueOf(value)) || name().equalsIgnoreCase(String.valueOf(value));
    }
}
