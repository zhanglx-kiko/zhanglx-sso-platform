package com.zhanglx.sso.core.exception;

import com.zhanglx.sso.common.ResultCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 核心基础设施错误码。
 */
@Getter
@RequiredArgsConstructor
public enum CoreErrorCode implements ErrorCode {

    ENUM_CODE_UNSUPPORTED(ResultCode.BAD_REQUEST.getCode(), "core.enum.code.unsupported"),
    LOCAL_DATE_TIME_PARSE_FAILED(ResultCode.BAD_REQUEST.getCode(), "core.local-date-time.parse.failed"),
    LONG_PARSE_FAILED(ResultCode.BAD_REQUEST.getCode(), "core.long.parse.failed"),
    TREE_EXPORT_FAILED(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "core.tree.export.failed"),
    TREE_IMPORT_FAILED(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "core.tree.import.failed");

    private final Integer code;
    private final String messageKey;
}
