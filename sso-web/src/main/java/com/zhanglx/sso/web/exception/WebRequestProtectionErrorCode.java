package com.zhanglx.sso.web.exception;

import com.zhanglx.sso.common.ResultCode;
import com.zhanglx.sso.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WebRequestProtectionErrorCode implements ErrorCode {

    REPEAT_SUBMIT(ResultCode.TOO_MANY_REQUESTS.getCode(), "repeat.submit"),
    RATE_LIMITED(ResultCode.TOO_MANY_REQUESTS.getCode(), "request.rate.limit.exceeded");

    private final Integer code;
    private final String messageKey;
}
