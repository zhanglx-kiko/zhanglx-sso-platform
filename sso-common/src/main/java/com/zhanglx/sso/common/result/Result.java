package com.zhanglx.sso.common.result;

import com.zhanglx.sso.common.ResultCode;
import lombok.*;

import java.io.Serializable;

/**
 * 统一返回结果对象。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Result<T> implements Serializable {

    /**
     * 编码。
     */
    private Integer code;

    /**
     * 消息。
     */
    private String msg;

    /**
     * 数据。
     */
    private T data;

    public static Result<Void> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), msg, data);
    }

    public static Result<Void> error(String msg) {
        return new Result<>(ResultCode.INTERNAL_SERVER_ERROR.getCode(), msg, null);
    }

    public static Result<Void> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static Result<Void> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    public static <T> Result<T> error(String msg, T data) {
        return new Result<>(ResultCode.INTERNAL_SERVER_ERROR.getCode(), msg, data);
    }

}