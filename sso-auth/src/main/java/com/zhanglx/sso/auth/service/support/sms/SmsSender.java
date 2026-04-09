package com.zhanglx.sso.auth.service.support.sms;

public interface SmsSender {

    void send(String phoneNumber, String content);
}
