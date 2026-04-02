package com.zhanglx.sso.core.exception;

import com.zhanglx.sso.common.ResultCode;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Locale;

@Getter
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        this(resolveCode(message), message);
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        this(resolveCode(message), message, cause);
    }

    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(ResultCode.BAD_REQUEST.getCode(), message);
    }

    public static BusinessException badRequest(String message, Throwable cause) {
        return new BusinessException(ResultCode.BAD_REQUEST.getCode(), message, cause);
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException(ResultCode.UNAUTHORIZED.getCode(), message);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(ResultCode.FORBIDDEN.getCode(), message);
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(ResultCode.NOT_FOUND.getCode(), message);
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(ResultCode.CONFLICT.getCode(), message);
    }

    public static BusinessException internalError(String message) {
        return new BusinessException(ResultCode.INTERNAL_SERVER_ERROR.getCode(), message);
    }

    public static BusinessException internalError(String message, Throwable cause) {
        return new BusinessException(ResultCode.INTERNAL_SERVER_ERROR.getCode(), message, cause);
    }

    public static BusinessException badGateway(String message) {
        return new BusinessException(ResultCode.BAD_GATEWAY.getCode(), message);
    }

    public static BusinessException badGateway(String message, Throwable cause) {
        return new BusinessException(ResultCode.BAD_GATEWAY.getCode(), message, cause);
    }

    private static Integer resolveCode(String message) {
        if (message == null || message.isBlank()) {
            return ResultCode.BAD_REQUEST.getCode();
        }

        return switch (message) {
            case "login.required",
                    "business.user.password.error",
                    "user.password.error",
                    "business.user.token.expired",
                    "business.user.token.invalid" -> ResultCode.UNAUTHORIZED.getCode();
            case "permission.denied",
                    "business.permission.not.enough",
                    "user.account.disabled",
                    "user.account.locked" -> ResultCode.FORBIDDEN.getCode();
            case "business.resource.not.found",
                    "business.user.not.found",
                    "user.info.not.found" -> ResultCode.NOT_FOUND.getCode();
            case "business.data.duplicate" -> ResultCode.CONFLICT.getCode();
            case "system.busy",
                    "system.internal.error",
                    "system.service.unavailable",
                    "system.timeout",
                    "technical.database.error",
                    "technical.network.error",
                    "technical.file.not.found",
                    "technical.file.upload.failed",
                    "technical.cache.error",
                    "technical.third.party.error",
                    "password.encryption.failed",
                    "password.encryption.timed.out",
                    "password.verification.timed.out" -> ResultCode.INTERNAL_SERVER_ERROR.getCode();
            default -> inferCodeFromMessage(message);
        };
    }

    private static Integer inferCodeFromMessage(String message) {
        String normalized = message.toLowerCase(Locale.ROOT);
        if (normalized.contains("not found") || message.contains("不存在") || message.contains("找不到")) {
            return ResultCode.NOT_FOUND.getCode();
        }
        if (normalized.contains("duplicate") || message.contains("重复") || message.contains("已存在")) {
            return ResultCode.CONFLICT.getCode();
        }
        if (message.contains("无权限") || message.contains("权限不足") || message.contains("禁用") || message.contains("锁定")) {
            return ResultCode.FORBIDDEN.getCode();
        }
        if (normalized.contains("timeout") || message.contains("超时") || message.contains("异常")) {
            return ResultCode.INTERNAL_SERVER_ERROR.getCode();
        }
        return ResultCode.BAD_REQUEST.getCode();
    }

}
