package com.zhanglx.sso.core.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Arrays;

/**
 * 统一业务异常。
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {
    /**
     * 编码。
     */
    private final Integer code;
    /**
     * 消息键。
     */
    private final String messageKey;
    /**
     * args。
     */
    private final transient Object[] args;

    public BusinessException(String messageKey, Object... args) {
        this(CommonErrorCode.BAD_REQUEST.getCode(), messageKey, null, args);
    }

    public BusinessException(String messageKey, Throwable cause) {
        this(CommonErrorCode.BAD_REQUEST.getCode(), messageKey, cause);
    }

    public BusinessException(Integer code, String messageKey, Object... args) {
        this(code, messageKey, null, args);
    }

    public BusinessException(Integer code, String messageKey, Throwable cause) {
        this(code, messageKey, cause, (Object[]) null);
    }

    public BusinessException(Integer code, String messageKey, Throwable cause, Object... args) {
        super(messageKey, cause);
        this.code = code;
        this.messageKey = messageKey;
        this.args = args == null ? new Object[0] : Arrays.copyOf(args, args.length);
    }

    public BusinessException(ErrorCode errorCode, Object... args) {
        this(errorCode.getCode(), errorCode.getMessageKey(), null, args);
    }

    public BusinessException(ErrorCode errorCode, Throwable cause, Object... args) {
        this(errorCode.getCode(), errorCode.getMessageKey(), cause, args);
    }

    public static BusinessException of(ErrorCode errorCode, Object... args) {
        return new BusinessException(errorCode, args);
    }

    public static BusinessException of(ErrorCode errorCode, Throwable cause, Object... args) {
        return new BusinessException(errorCode, cause, args);
    }

    public static BusinessException badRequest(String messageKey) {
        return new BusinessException(CommonErrorCode.BAD_REQUEST.getCode(), messageKey);
    }

    public static BusinessException badRequest(String messageKey, Throwable cause) {
        return new BusinessException(CommonErrorCode.BAD_REQUEST.getCode(), messageKey, cause);
    }

    public static BusinessException badRequest(String messageKey, Object... args) {
        return new BusinessException(CommonErrorCode.BAD_REQUEST.getCode(), messageKey, args);
    }

    public static BusinessException unauthorized(String messageKey) {
        return new BusinessException(CommonErrorCode.UNAUTHORIZED.getCode(), messageKey);
    }

    public static BusinessException unauthorized(String messageKey, Object... args) {
        return new BusinessException(CommonErrorCode.UNAUTHORIZED.getCode(), messageKey, args);
    }

    public static BusinessException forbidden(String messageKey) {
        return new BusinessException(CommonErrorCode.FORBIDDEN.getCode(), messageKey);
    }

    public static BusinessException forbidden(String messageKey, Object... args) {
        return new BusinessException(CommonErrorCode.FORBIDDEN.getCode(), messageKey, args);
    }

    public static BusinessException notFound(String messageKey) {
        return new BusinessException(CommonErrorCode.NOT_FOUND.getCode(), messageKey);
    }

    public static BusinessException notFound(String messageKey, Object... args) {
        return new BusinessException(CommonErrorCode.NOT_FOUND.getCode(), messageKey, args);
    }

    public static BusinessException conflict(String messageKey) {
        return new BusinessException(CommonErrorCode.CONFLICT.getCode(), messageKey);
    }

    public static BusinessException conflict(String messageKey, Object... args) {
        return new BusinessException(CommonErrorCode.CONFLICT.getCode(), messageKey, args);
    }

    public static BusinessException internalError(String messageKey) {
        return new BusinessException(CommonErrorCode.INTERNAL_ERROR.getCode(), messageKey);
    }

    public static BusinessException internalError(String messageKey, Throwable cause) {
        return new BusinessException(CommonErrorCode.INTERNAL_ERROR.getCode(), messageKey, cause);
    }

    public static BusinessException internalError(String messageKey, Object... args) {
        return new BusinessException(CommonErrorCode.INTERNAL_ERROR.getCode(), messageKey, args);
    }

    public static BusinessException badGateway(String messageKey) {
        return new BusinessException(CommonErrorCode.BAD_GATEWAY.getCode(), messageKey);
    }

    public static BusinessException badGateway(String messageKey, Throwable cause) {
        return new BusinessException(CommonErrorCode.BAD_GATEWAY.getCode(), messageKey, cause);
    }

    public static BusinessException badGateway(String messageKey, Object... args) {
        return new BusinessException(CommonErrorCode.BAD_GATEWAY.getCode(), messageKey, args);
    }

}