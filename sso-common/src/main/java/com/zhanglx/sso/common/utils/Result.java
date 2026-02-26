package com.zhanglx.sso.common.utils;

import java.io.Serializable;
import java.util.Objects;

public class Result<T> implements Serializable {
    private Integer code;
    private String msg;
    private T data;

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Result() {
    }

    // 无数据成功返回
    public static Result<Void> success() {
        return new Result<>(200, "操作成功", null);
    }

    // 带数据成功返回
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    // 自定义消息+带数据成功返回
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(200, msg, data);
    }

    // 无数据错误返回
    public static Result<Void> error(String msg) {
        return new Result<>(500, msg, null);
    }

    // 自定义状态码+消息错误返回
    public static Result<Void> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    // 可选：带数据的错误返回
    public static <T> Result<T> error(String msg, T data) {
        return new Result<>(500, msg, data);
    }

    public Integer getCode() {
        return code;
    }

    public Result<T> setCode(Integer code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public Result<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public T getData() {
        return data;
    }

    public Result<T> setData(T data) {
        this.data = data;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Result<?> result = (Result<?>) o;
        return Objects.equals(code, result.code) && Objects.equals(msg, result.msg) && Objects.equals(data, result.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, msg, data);
    }

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}