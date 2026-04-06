package com.zhanglx.sso.auth.domain.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "member.verification-code")
public class MemberVerificationCodeProperties {

    private long expireSeconds = 300L;

    private long resendIntervalSeconds = 60L;

    private int codeLength = 6;

    private boolean mockSendEnabled = true;
}
