package com.zhanglx.sso.auth.domain.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "sso.sms.sms-chinese")
public class SmsChineseProperties {

    private boolean enabled = true;

    private String uid;

    private String key;

    private String sendUrl = "https://utf8api.smschinese.cn/";

    private int connectTimeoutMillis = 5000;

    private int readTimeoutMillis = 5000;
}
