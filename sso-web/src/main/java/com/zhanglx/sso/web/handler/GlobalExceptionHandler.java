package com.zhanglx.sso.web.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.zhanglx.sso.core.domain.result.Result;
import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.web.utils.I18nUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/11 10:43
 * @ClassName: GlobalExceptionHandler
 * @Description:
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.zhanglx")
public class GlobalExceptionHandler {

    public GlobalExceptionHandler() {
        log.info("GlobalExceptionHandler 初始化完成");
    }

    /**
     * 1. 拦截自定义业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常: code={}, msg={}", e.getCode(), e.getMessage());
        String message = I18nUtils.getMessage(e.getMessage());
        return Result.error(e.getCode(), message != null ? message : e.getMessage());
    }

    /**
     * 2. 拦截 Sa-Token 未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handleNotLoginException(NotLoginException e) {
        log.warn("未登录访问：{}", e.getMessage());
        return Result.error(401, I18nUtils.getMessage("login.required"));
    }

    /**
     * 3. 拦截 Sa-Token 权限不足异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public Result<Void> handleNotPermissionException(NotPermissionException e) {
        log.warn("无权限访问：{}", e.getPermission());
        return Result.error(403, I18nUtils.getMessage("permission.denied"));
    }

    /**
     * 4. 拦截 Sa-Token 角色不足异常
     */
    @ExceptionHandler(NotRoleException.class)
    public Result<Void> handleNotRoleException(NotRoleException e) {
        log.warn("无角色访问：{}", e.getRole());
        return Result.error(403, I18nUtils.getMessage("permission.denied"));
    }

    /**
     * 5. 拦截参数校验异常 (@RequestBody @Validated)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败：{}", message);
        return Result.error(400, I18nUtils.getMessage("parameter.error") + ": " + message);
    }

    /**
     * 6. 拦截参数校验异常 (@RequestParam @Validated)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败：{}", message);
        return Result.error(400, I18nUtils.getMessage("parameter.error") + ": " + message);
    }

    /**
     * 7. 拦截绑定异常 (@ModelAttribute @Validated)
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败：{}", message);
        return Result.error(400, I18nUtils.getMessage("parameter.error") + ": " + message);
    }

    /**
     * 8. 拦截 404 异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("资源不存在：{}", e.getRequestURL());
        return Result.error(404, I18nUtils.getMessage("business.resource.not.found"));
    }

    /**
     * 9. 拦截请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持：{}", e.getMethod());
        return Result.error(405, I18nUtils.getMessage("method.not.allowed") + ": " + e.getMethod());
    }

    /**
     * 10. 拦截全局系统异常 (兜底)
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("系统内部异常", e);
        return Result.error(500, I18nUtils.getMessage("system.busy"));
    }

}
