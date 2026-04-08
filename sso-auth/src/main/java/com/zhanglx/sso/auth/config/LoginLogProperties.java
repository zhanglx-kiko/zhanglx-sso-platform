package com.zhanglx.sso.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 登录日志配置。
 * 登录日志量级可控，但仍然必须异步化，避免高峰登录时把主链路绑死在 MySQL 上。
 */
@Data
@ConfigurationProperties(prefix = "sso.log.login")
public class LoginLogProperties {

    private boolean enabled = true;

    private int corePoolSize = 1;

    private int maxPoolSize = 2;

    private int queueCapacity = 2000;

    private int keepAliveSeconds = 60;
}
