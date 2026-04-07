package com.zhanglx.sso.auth.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.zhanglx.sso.core.enums.IBaseEnum;
import com.zhanglx.sso.core.enums.IStringBaseEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SocialIdentityTypeEnum implements IStringBaseEnum<String> {

    WECHAT_OPEN("WECHAT_OPEN", "微信开放平台"),
    WX_MINI("WX_MINI", "微信小程序");

    @JsonValue
    private final String code;

    private final String description;

    public static SocialIdentityTypeEnum fromCode(String code) {
        return IBaseEnum.fromCode(code, SocialIdentityTypeEnum.class);
    }

}
