package com.zhanglx.sso.auth.service.support.sms;

import com.zhanglx.sso.auth.domain.properties.SmsChineseProperties;
import com.zhanglx.sso.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsChineseSmsSender implements SmsSender {

    private final SmsChineseProperties properties;

    @Override
    public void send(String phoneNumber, String content) {
        validateProperties();
        HttpClient httpClient = createHttpClient();
        PostMethod postMethod = new PostMethod(properties.getSendUrl());
        try {
            postMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            postMethod.getParams().setContentCharset("UTF-8");
            postMethod.setRequestBody(new NameValuePair[]{
                    new NameValuePair("Uid", properties.getUid().trim()),
                    new NameValuePair("Key", properties.getKey().trim()),
                    new NameValuePair("smsMob", phoneNumber),
                    new NameValuePair("smsText", content)
            });

            int httpStatus = httpClient.executeMethod(postMethod);
            String responseBody = postMethod.getResponseBodyAsString();
            if (httpStatus != 200) {
                log.warn("smschinese send failed by http status, status={}, phone={}, body={}",
                        httpStatus,
                        maskPhone(phoneNumber),
                        responseBody);
                throw BusinessException.badGateway("technical.sms.response.invalid");
            }

            int providerCode = parseProviderCode(responseBody);
            if (providerCode > 0) {
                log.info("smschinese send success, phone={}, count={}", maskPhone(phoneNumber), providerCode);
                return;
            }

            log.warn("smschinese send rejected, phone={}, providerCode={}, body={}",
                    maskPhone(phoneNumber),
                    providerCode,
                    responseBody);
            throw mapProviderException(providerCode);
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            log.error("failed to call smschinese sms api, phone={}", maskPhone(phoneNumber), e);
            throw BusinessException.badGateway("technical.sms.service.error", e);
        } finally {
            postMethod.releaseConnection();
        }
    }

    private HttpClient createHttpClient() {
        HttpClient httpClient = new HttpClient();
        HttpConnectionManagerParams params = httpClient.getHttpConnectionManager().getParams();
        params.setConnectionTimeout(Math.max(1000, properties.getConnectTimeoutMillis()));
        params.setSoTimeout(Math.max(1000, properties.getReadTimeoutMillis()));
        return httpClient;
    }

    private void validateProperties() {
        if (!properties.isEnabled()) {
            throw BusinessException.internalError("technical.sms.channel.disabled");
        }
        if (!StringUtils.hasText(properties.getUid())
                || !StringUtils.hasText(properties.getKey())
                || !StringUtils.hasText(properties.getSendUrl())) {
            throw BusinessException.internalError("technical.sms.config.invalid");
        }
    }

    private int parseProviderCode(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            throw BusinessException.badGateway("technical.sms.response.invalid");
        }

        try {
            return Integer.parseInt(responseBody.trim());
        } catch (NumberFormatException e) {
            log.warn("invalid smschinese response body, body={}", responseBody, e);
            throw BusinessException.badGateway("technical.sms.response.invalid", e);
        }
    }

    private BusinessException mapProviderException(int providerCode) {
        return switch (providerCode) {
            case -1 -> BusinessException.internalError("technical.sms.account.not.found");
            case -2, -21 -> BusinessException.internalError("technical.sms.key.invalid");
            case -3 -> BusinessException.badGateway("technical.sms.balance.insufficient");
            case -4, -41 -> BusinessException.badRequest("member.phone.invalid");
            case -6 -> BusinessException.badGateway("technical.sms.ip.restricted");
            case -11 -> BusinessException.badGateway("technical.sms.account.disabled");
            case -14 -> BusinessException.internalError("technical.sms.content.invalid");
            case -42 -> BusinessException.internalError("technical.sms.content.empty");
            case -51, -52 -> BusinessException.internalError("technical.sms.signature.invalid");
            default -> BusinessException.badGateway("technical.sms.service.error");
        };
    }

    private String maskPhone(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber) || phoneNumber.length() < 7) {
            return phoneNumber;
        }
        return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(phoneNumber.length() - 4);
    }
}
