package com.zhanglx.sso.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 登录日志独立线程池。
 */
@Configuration
@EnableConfigurationProperties(LoginLogProperties.class)
public class LoginLogAsyncConfig {

    @Bean("loginLogTaskExecutor")
    public ThreadPoolTaskExecutor loginLogTaskExecutor(LoginLogProperties properties) {
        int corePoolSize = Math.max(1, properties.getCorePoolSize());
        int maxPoolSize = Math.max(corePoolSize, properties.getMaxPoolSize());
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("login-log-");
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(Math.max(128, properties.getQueueCapacity()));
        executor.setKeepAliveSeconds(properties.getKeepAliveSeconds());
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        executor.initialize();
        return executor;
    }
}
