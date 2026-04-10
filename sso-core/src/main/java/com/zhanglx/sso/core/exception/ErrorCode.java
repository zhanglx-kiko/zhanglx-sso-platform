package com.zhanglx.sso.core.exception;

/**
 * 业务错误码抽象。
 */
public interface ErrorCode {

    Integer getCode();

    String getMessageKey();

}