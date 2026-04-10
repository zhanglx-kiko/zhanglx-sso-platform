package com.zhanglx.sso.auth.exception;

import com.zhanglx.sso.common.ResultCode;
import com.zhanglx.sso.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 用户域错误码定义。
 */
@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_ACCOUNT_EMPTY(ResultCode.BAD_REQUEST.getCode(), "user.account.empty"),
    USER_OPEN_ID_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "user.openId.cannot.be.blank"),
    USERNAME_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "user.username.cannot.be.blank"),
    USER_INFO_NOT_FOUND(ResultCode.NOT_FOUND.getCode(), "user.info.not.found"),
    USER_NOT_FOUND(ResultCode.NOT_FOUND.getCode(), "user.not.exists"),
    BUSINESS_USER_NOT_FOUND(ResultCode.NOT_FOUND.getCode(), "business.user.not.found"),
    USER_PASSWORD_ERROR(ResultCode.UNAUTHORIZED.getCode(), "user.password.error"),
    USER_OLD_PASSWORD_ERROR(ResultCode.UNAUTHORIZED.getCode(), "user.old.password.error"),
    USER_ACCOUNT_DISABLED(ResultCode.FORBIDDEN.getCode(), "user.account.disabled"),
    USER_PHONE_NOT_BOUND(ResultCode.BAD_REQUEST.getCode(), "user.phone.not.bound"),
    USERNAME_ALREADY_EXISTS(ResultCode.CONFLICT.getCode(), "user.username.already.exists");
    /**
     * 验证码。
     */
    private final Integer code;
    /**
     * 消息键。
     */
    private final String messageKey;

}
