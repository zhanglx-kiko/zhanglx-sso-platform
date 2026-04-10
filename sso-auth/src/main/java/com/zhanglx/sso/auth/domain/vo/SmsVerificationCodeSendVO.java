package com.zhanglx.sso.auth.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短信验证码发送结果视图对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SmsVerificationCodeSendVO", description = "短信验证码发送结果")
public class SmsVerificationCodeSendVO {

    /**
     * 脱敏手机号。
     */
    @Schema(description = "脱敏手机号", example = "138****8000")
    private String maskedPhoneNumber;

    /**
     * 验证码有效期，单位秒。
     */
    @Schema(description = "验证码有效期，单位秒", example = "180")
    private long expireSeconds;

    /**
     * 重新发送间隔，单位秒。
     */
    @Schema(description = "重新发送间隔，单位秒", example = "60")
    private long resendIntervalSeconds;
}
