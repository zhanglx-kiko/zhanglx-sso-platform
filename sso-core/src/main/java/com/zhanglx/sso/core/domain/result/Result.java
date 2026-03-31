package com.zhanglx.sso.core.domain.result;

import com.zhanglx.sso.common.ResultCode;
import lombok.*;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Result<T> implements Serializable {

    private Integer code;
    private String msg;
    private T data;

    // 无数据成功返回
    public static Result<Void> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    // 带数据成功返回
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    // 自定义消息 + 带数据成功返回
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), msg, data);
    }

    // 无数据错误返回
    public static Result<Void> error(String msg) {
        return new Result<>(ResultCode.ERROR.getCode(), msg, null);
    }

    // 自定义状态码 + 消息错误返回
    public static Result<Void> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    // 使用枚举定义错误
    public static Result<Void> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    // 可选：带数据的错误返回
    public static <T> Result<T> error(String msg, T data) {
        return new Result<>(ResultCode.ERROR.getCode(), msg, data);
    }

}