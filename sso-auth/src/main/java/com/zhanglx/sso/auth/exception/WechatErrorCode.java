package com.zhanglx.sso.auth.exception;

import com.zhanglx.sso.common.ResultCode;
import com.zhanglx.sso.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 微信认证域错误码定义。
 */
@Getter
@RequiredArgsConstructor
public enum WechatErrorCode implements ErrorCode {

    WECHAT_CODE_REQUIRED(ResultCode.BAD_REQUEST.getCode(), "wechat.code.cannot.be.blank"),
    WECHAT_LOGIN_FAILED(ResultCode.BAD_REQUEST.getCode(), "wechat.login.failed"),
    WECHAT_SERVICE_ERROR(ResultCode.BAD_GATEWAY.getCode(), "technical.wechat.service.error");

    private final Integer code;
    private final String messageKey;
}
