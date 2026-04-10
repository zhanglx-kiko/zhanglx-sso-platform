package com.zhanglx.sso.core.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Map;

/**
 * 作者：Zhang L X
 * 创建时间：2026/2/26 18:20
 * 类名：VirtualThreadConfig
 * 说明：适配 JDK 25 虚拟线程的异步配置
 */
@EnableAsync // 必须加上，否则 @Async 注解不生效
@Configuration
public class VirtualThreadConfig {

    /**
     * 注册任务装饰器后，Spring Boot 自动装配的虚拟线程执行器会自动使用它。
     * 用于完成主线程到虚拟线程的上下文传递，例如 Sa-Token 的线程上下文数据。
     */
    @Bean
    public TaskDecorator contextCopyingDecorator() {
        return runnable -> {
            RequestAttributes context = RequestContextHolder.getRequestAttributes();
            Map<String, String> mdcContext = MDC.getCopyOfContextMap();
            return () -> {
                try {
                    if (context != null) {
                        RequestContextHolder.setRequestAttributes(context);
                    }
                    if (mdcContext != null && !mdcContext.isEmpty()) {
                        MDC.setContextMap(mdcContext);
                    } else {
                        MDC.clear();
                    }
                    runnable.run();
                } finally {
                    RequestContextHolder.resetRequestAttributes();
                    MDC.clear();
                }
            };
        };
    }

}
