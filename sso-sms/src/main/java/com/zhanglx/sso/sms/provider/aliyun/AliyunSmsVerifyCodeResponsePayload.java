package com.zhanglx.sso.sms.provider.aliyun;

import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody;
import lombok.Builder;
import lombok.Getter;

/**
 * 阿里云短信验证码响应载荷类型。
 */
@Getter
@Builder
public class AliyunSmsVerifyCodeResponsePayload {
    /**
     * 响应状态码。
     */
    private final Integer httpStatus;
    /**
     * 访问拒绝详情。
     */
    private final String accessDeniedDetail;
    /**
     * mesSage。
     */
    private final String message;
    /**
     * 请求标识。
     */
    private final String requestId;
    /**
     * 验证码。
     */
    private final String code;
    /**
     * success。
     */
    private final Boolean success;
    /**
     * model。
     */
    private final AliyunSmsVerifyCodeModel model;

    public static AliyunSmsVerifyCodeResponsePayload from(SendSmsVerifyCodeResponse response) {
        if (response == null) {
            return AliyunSmsVerifyCodeResponsePayload.builder().build();
        }

        SendSmsVerifyCodeResponseBody body = response.getBody();
        SendSmsVerifyCodeResponseBody.Model responseModel = body == null ? null : body.getModel();
        return AliyunSmsVerifyCodeResponsePayload.builder()
                .httpStatus(response.getStatusCode())
                .accessDeniedDetail(body == null ? null : body.getAccessDeniedDetail())
                .message(body == null ? null : body.getMessage())
                .requestId(body == null ? null : body.getRequestId())
                .code(body == null ? null : body.getCode())
                .success(body == null ? null : body.getSuccess())
                .model(responseModel == null ? null : AliyunSmsVerifyCodeModel.builder()
                                                      .verifyCode(responseModel.getVerifyCode())
                                                      .requestId(responseModel.getRequestId())
                                                      .outId(responseModel.getOutId())
                                                      .bizId(responseModel.getBizId())
                                                      .build())
                .build();
    }

    @Getter
    @Builder
    public static class AliyunSmsVerifyCodeModel {
        /**
         * verifyCode。
         */
        private final String verifyCode;
        /**
         * 请求标识。
         */
        private final String requestId;
        /**
         * 外部标识。
         */
        private final String outId;
        /**
         * 业务标识。
         */
        private final String bizId;
    }
}