package com.zhanglx.sso.auth.exception;

import com.zhanglx.sso.common.ResultCode;
import com.zhanglx.sso.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 会员域错误码定义。
 */
@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {

    MEMBER_ACCOUNT_EMPTY(ResultCode.BAD_REQUEST.getCode(), "member.account.empty"),
    MEMBER_NOT_FOUND(ResultCode.NOT_FOUND.getCode(), "member.info.not.found"),
    MEMBER_PHONE_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "member.phone.cannot.be.blank"),
    MEMBER_VERIFICATION_CODE_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "member.verification.code.cannot.be.blank"),
    MEMBER_VERIFICATION_SCENE_INVALID(ResultCode.BAD_REQUEST.getCode(), "member.verification.scene.invalid"),
    MEMBER_PHONE_ALREADY_BOUND(ResultCode.CONFLICT.getCode(), "member.phone.already.bound"),
    MEMBER_PHONE_BIND_SAME_AS_CURRENT(ResultCode.BAD_REQUEST.getCode(), "member.phone.bind.same.as.current"),
    MEMBER_PHONE_NOT_CURRENT_BOUND(ResultCode.BAD_REQUEST.getCode(), "member.phone.not.current.bound"),
    MEMBER_PHONE_UPDATE_REQUIRES_VERIFICATION(ResultCode.BAD_REQUEST.getCode(), "member.phone.update.requires.verification");
    /**
     * 验证码。
     */
    private final Integer code;
    /**
     * 消息键。
     */
    private final String messageKey;

}
