package com.zhanglx.sso.sms.provider.aliyun;

import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AliyunSmsVerifyCodeResponsePayload {

    private final Integer httpStatus;

    private final String accessDeniedDetail;

    private final String message;

    private final String requestId;

    private final String code;

    private final Boolean success;

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

        private final String verifyCode;

        private final String requestId;

        private final String outId;

        private final String bizId;
    }
}
