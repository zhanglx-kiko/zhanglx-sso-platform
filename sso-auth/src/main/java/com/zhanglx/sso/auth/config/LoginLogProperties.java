package com.zhanglx.sso.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 登录日志配置。
 * 登录日志量级可控，但仍然必须异步化，避免高峰登录时把主链路绑死在 MySQL 上。
 */
/**
 * 登录日志配置属性。
 */

@Data
@ConfigurationProperties(prefix = "sso.log.login")
public class LoginLogProperties {

    /**
     * 是否启用登录日志。
     */
    private boolean enabled = true;


    /**
     * 异步线程池核心线程数。
     */
    private int corePoolSize = 1;


    /**
     * 异步线程池最大线程数。
     */
    private int maxPoolSize = 2;


    /**
     * 异步线程池队列容量。
     */
    private int queueCapacity = 2000;


    /**
     * 非核心线程空闲存活时间，单位为秒。
     */
    private int keepAliveSeconds = 60;
}
