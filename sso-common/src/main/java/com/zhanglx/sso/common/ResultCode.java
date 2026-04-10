package com.zhanglx.sso.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一结果码枚举。
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),

    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "请先登录"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    CONFLICT(409, "数据冲突"),
    UNPROCESSABLE_ENTITY(422, "请求无法处理"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    INTERNAL_SERVER_ERROR(500, "系统繁忙，请稍后再试"),
    BAD_GATEWAY(502, "上游服务异常"),
    SERVICE_UNAVAILABLE(503, "服务暂时不可用"),
    GATEWAY_TIMEOUT(504, "请求超时");

    /**
     * 编码。
     */
    private final Integer code;

    /**
     * 消息。
     */
    private final String message;

}