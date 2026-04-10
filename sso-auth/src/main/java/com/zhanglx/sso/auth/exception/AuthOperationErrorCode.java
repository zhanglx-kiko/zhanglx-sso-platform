package com.zhanglx.sso.auth.exception;

import com.zhanglx.sso.common.ResultCode;
import com.zhanglx.sso.core.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * AuthOperationErrorCode枚举。
 */
@Getter
@RequiredArgsConstructor
public enum AuthOperationErrorCode implements ErrorCode {

    REPEAT_SUBMIT(ResultCode.TOO_MANY_REQUESTS.getCode(), "repeat.submit"),
    RESET_CURRENT_USER_PASSWORD_FORBIDDEN(ResultCode.FORBIDDEN.getCode(), "user.self.reset.password.forbidden"),
    DISABLE_CURRENT_USER_FORBIDDEN(ResultCode.FORBIDDEN.getCode(), "user.self.disable.forbidden"),
    DELETE_CURRENT_USER_FORBIDDEN(ResultCode.FORBIDDEN.getCode(), "user.self.delete.forbidden"),
    REMOVE_CURRENT_USER_ROLE_BINDING_FORBIDDEN(ResultCode.FORBIDDEN.getCode(), "role.self.unbind.forbidden"),
    REDUCE_CURRENT_USER_ROLE_PERMISSION_FORBIDDEN(ResultCode.FORBIDDEN.getCode(), "role.self.permission.reduce.forbidden"),
    DISABLE_CURRENT_USER_ROLE_FORBIDDEN(ResultCode.FORBIDDEN.getCode(), "role.self.disable.forbidden"),
    DELETE_CURRENT_USER_ROLE_FORBIDDEN(ResultCode.FORBIDDEN.getCode(), "role.self.delete.forbidden");
    /**
     * 验证码。
     */
    private final Integer code;
    /**
     * 消息键。
     */
    private final String messageKey;

}