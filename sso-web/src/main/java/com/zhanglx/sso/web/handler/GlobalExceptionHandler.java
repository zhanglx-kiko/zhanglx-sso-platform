package com.zhanglx.sso.web.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.zhanglx.sso.common.result.Result;
import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.web.utils.I18nUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice(basePackages = "com.zhanglx")
public class GlobalExceptionHandler {

    private final I18nUtils i18nUtils;

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        if (e.getCode() != null && e.getCode() >= 500) {
            log.error("业务异常: code={}, msg={}", e.getCode(), e.getMessage(), e);
        } else {
            log.warn("业务异常: code={}, msg={}", e.getCode(), e.getMessage());
        }
        return Result.error(e.getCode(), resolveMessage(e.getMessage()));
    }

    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handleNotLoginException(NotLoginException e) {
        log.warn("未登录访问: {}", e.getMessage());
        return Result.error(401, resolveMessage("login.required"));
    }

    @ExceptionHandler(NotPermissionException.class)
    public Result<Void> handleNotPermissionException(NotPermissionException e) {
        log.warn("无权限访问: {}", e.getPermission());
        return Result.error(403, resolveMessage("permission.denied"));
    }

    @ExceptionHandler(NotRoleException.class)
    public Result<Void> handleNotRoleException(NotRoleException e) {
        log.warn("角色不足: {}", e.getRole());
        return Result.error(403, resolveMessage("permission.denied"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("请求体校验失败: {}", message);
        return Result.error(400, resolveMessage("parameter.error") + ": " + message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return Result.error(400, resolveMessage("parameter.error") + ": " + message);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败: {}", message);
        return Result.error(400, resolveMessage("parameter.error") + ": " + message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        BusinessException businessException = unwrapBusinessException(e);
        if (businessException != null) {
            return handleBusinessException(businessException);
        }

        log.warn("请求体解析失败: {}", e.getMessage());
        return Result.error(400, resolveMessage("parameter.error"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String name = e.getName() == null ? "parameter" : e.getName();
        String value = e.getValue() == null ? "null" : String.valueOf(e.getValue());
        log.warn("参数类型不匹配: {}={}", name, value);
        return Result.error(400, resolveMessage("parameter.error") + ": 参数 [" + name + "] 值 [" + value + "] 类型不正确");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数: {}", e.getParameterName());
        return Result.error(400, resolveMessage("parameter.error") + ": 缺少参数 [" + e.getParameterName() + "]");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("上传文件过大: {}", e.getMessage());
        return Result.error(400, "上传文件大小超出限制");
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("接口不存在: {}", e.getRequestURL());
        return Result.error(404, resolveMessage("business.resource.not.found"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("资源不存在: {}", e.getResourcePath());
        return Result.error(404, resolveMessage("business.resource.not.found"));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持: {}", e.getMethod());
        return Result.error(405, resolveMessage("method.not.allowed") + ": " + e.getMethod());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系统内部异常", e);
        return Result.error(500, resolveMessage("system.busy"));
    }

    private String resolveMessage(String messageKeyOrMessage) {
        String resolved = i18nUtils.getMessage(messageKeyOrMessage);
        return resolved == null || resolved.isBlank() ? messageKeyOrMessage : resolved;
    }

    private BusinessException unwrapBusinessException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof BusinessException businessException) {
                return businessException;
            }
            current = current.getCause();
        }
        return null;
    }

}
