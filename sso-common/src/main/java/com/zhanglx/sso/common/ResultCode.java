package com.zhanglx.sso.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/16 17:53
 * @ClassName: ResultCode
 * @Description: 全局错误码枚举
 * 格式说明：
 * 2xx - 成功
 * 4xx - 客户端错误
 * 5xx - 服务端错误
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // ========== 成功 ==========
    SUCCESS(200, "操作成功"),

    // ========== 客户端错误 ==========
    ERROR(500, "操作失败");

    private final Integer code;
    private final String message;

}
