package com.zhanglx.sso.core.exception;

import com.zhanglx.sso.common.ResultCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 通用错误码定义，按 HTTP/通用业务语义提供默认 code 与 messageKey。
 */
@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    BAD_REQUEST(ResultCode.BAD_REQUEST.getCode(), "parameter.error"),
    DATA_INVALID(ResultCode.BAD_REQUEST.getCode(), "business.data.invalid"),
    UNAUTHORIZED(ResultCode.UNAUTHORIZED.getCode(), "login.required"),
    FORBIDDEN(ResultCode.FORBIDDEN.getCode(), "permission.denied"),
    NOT_FOUND(ResultCode.NOT_FOUND.getCode(), "business.resource.not.found"),
    CONFLICT(ResultCode.CONFLICT.getCode(), "business.data.duplicate"),
    INTERNAL_ERROR(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "system.internal.error"),
    SYSTEM_BUSY(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "system.busy"),
    BAD_GATEWAY(ResultCode.BAD_GATEWAY.getCode(), "technical.third.party.error");

    private final Integer code;
    private final String messageKey;

}
