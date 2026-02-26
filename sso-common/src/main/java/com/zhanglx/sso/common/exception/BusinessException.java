package com.zhanglx.sso.common.exception;

import lombok.Getter;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/11 10:37
 * @ClassName: BusinessException
 * @Description: 全局业务异常 所有的逻辑错误都抛出此异常，由 GlobalExceptionHandler 统一捕获
 */
@Getter
public class BusinessException extends RuntimeException {

    // 错误码
    private Integer code;

    // 错误信息
    private String message;

    public BusinessException(String message) {
        this.code = 500;
        this.message = message;
    }

    public BusinessException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
