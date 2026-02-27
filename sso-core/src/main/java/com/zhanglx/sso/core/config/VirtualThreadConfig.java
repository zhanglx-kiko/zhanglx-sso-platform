package com.zhanglx.sso.core.config;

import org.springframework.boot.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/26 18:20
 * @ClassName: VirtualThreadConfig
 * @Description:
 */
@Configuration
public class VirtualThreadConfig {

    /**
     * 替换Spring默认的异步执行器为虚拟线程池
     */
    @Bean
    public Executor applicationTaskExecutor() {
        // Spring ThreadPoolTaskExecutor适配虚拟线程
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 使用虚拟线程工厂
        executor.setThreadFactory(Executors.defaultThreadFactory());
        executor.setVirtualThreads(true); // Spring Boot4.0+ 核心配置：启用虚拟线程
        executor.setThreadNamePrefix("app-virtual-thread-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(5);
        return executor;
    }

    /**
     * Web容器使用虚拟线程（Tomcat为例，Spring Boot4.0默认内嵌Tomcat10.1+）
     */
    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerCustomizer() {
        return protocolHandler -> {
            // Tomcat启用虚拟线程执行器
            protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        };
    }

}
