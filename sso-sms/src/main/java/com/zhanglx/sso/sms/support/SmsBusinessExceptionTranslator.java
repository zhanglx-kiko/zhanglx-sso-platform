package com.zhanglx.sso.sms.support;

import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.sms.enums.SmsProviderType;
import com.zhanglx.sso.sms.model.SmsSendResult;

import java.util.Locale;

public final class SmsBusinessExceptionTranslator {

    /**
     * 私有构造方法，禁止外部实例化。
     */
    private SmsBusinessExceptionTranslator() {
    }

    public static BusinessException translate(SmsSendResult result) {
        if (result == null) {
            return BusinessException.badGateway("technical.sms.send.failed");
        }

        if (result.getProviderType() == SmsProviderType.ALIYUN) {
            return translateAliyun(result);
        }
        return translateSmsChinese(result);
    }

    /**
     * 处理内部辅助逻辑。
     */
    private static BusinessException translateAliyun(SmsSendResult result) {
        String providerCode = normalize(result.getProviderCode());
        return switch (providerCode) {
            case "MOBILE_NUMBER_ILLEGAL" -> BusinessException.badRequest("member.phone.invalid");
            case "BUSINESS_LIMIT_CONTROL" ->
                    BusinessException.badGateway("technical.sms.provider.daily.limit.exceeded");
            case "FREQUENCY_FAIL" -> BusinessException.badRequest("technical.sms.provider.frequency.fail");
            case "INVALID_PARAMETERS" -> BusinessException.internalError("technical.sms.request.invalid");
            case "FUNCTION_NOT_OPENED" -> BusinessException.internalError("technical.sms.aliyun.function.not.opened");
            case "ACCESS_DENIED", "NO_PERMISSION", "NO_AUTHORITY" ->
                    BusinessException.internalError("technical.sms.aliyun.access.denied");
            case "CHANNEL_DISABLED" -> BusinessException.internalError("technical.sms.provider.disabled");
            case "CONFIG_INVALID" -> BusinessException.internalError("technical.sms.aliyun.config.invalid");
            case "TEMPLATE_NOT_CONFIGURED" -> BusinessException.internalError("technical.sms.template.not.configured");
            case "TEMPLATE_PARAM_INVALID" -> BusinessException.internalError("technical.sms.template.param.invalid");
            default -> BusinessException.badGateway("technical.sms.provider.unknown");
        };
    }

    /**
     * 处理内部辅助逻辑。
     */
    private static BusinessException translateSmsChinese(SmsSendResult result) {
        String providerCode = normalize(result.getProviderCode());
        return switch (providerCode) {
            case "-4", "-41" -> BusinessException.badRequest("member.phone.invalid");
            case "-6" -> BusinessException.internalError("technical.sms.smschinese.ip.restricted");
            case "-1", "-2", "-21" -> BusinessException.internalError("technical.sms.smschinese.config.invalid");
            case "-3" -> BusinessException.badGateway("technical.sms.smschinese.balance.insufficient");
            case "-11", "CHANNEL_DISABLED" -> BusinessException.internalError("technical.sms.provider.disabled");
            case "-14", "-42", "-51", "-52", "TEMPLATE_NOT_CONFIGURED" ->
                    BusinessException.internalError("technical.sms.template.not.configured");
            case "CONFIG_INVALID" -> BusinessException.internalError("technical.sms.smschinese.config.invalid");
            case "TEMPLATE_PARAM_INVALID" -> BusinessException.internalError("technical.sms.template.param.invalid");
            default -> BusinessException.badGateway("technical.sms.send.failed");
        };
    }

    /**
     * 处理内部辅助逻辑。
     */
    private static String normalize(String providerCode) {
        if (providerCode == null) {
            return "";
        }
        return providerCode.trim().toUpperCase(Locale.ROOT);
    }
}