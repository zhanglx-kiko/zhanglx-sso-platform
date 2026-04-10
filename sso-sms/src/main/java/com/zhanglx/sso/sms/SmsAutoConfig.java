package com.zhanglx.sso.sms;

import com.zhanglx.sso.sms.properties.SmsProperties;
import com.zhanglx.sso.sms.properties.SmsVerificationCodeProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan(basePackages = "com.zhanglx.sso.sms")
@EnableConfigurationProperties({SmsProperties.class, SmsVerificationCodeProperties.class})
public class SmsAutoConfig {
}
