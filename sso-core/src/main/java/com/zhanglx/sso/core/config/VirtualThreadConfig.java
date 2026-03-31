package com.zhanglx.sso.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/26 18:20
 * @ClassName: VirtualThreadConfig
 * @Description: 适配 JDK 25 虚拟线程的异步配置
 */
@EnableAsync // 必须加上，否则 @Async 注解不生效
@Configuration
public class VirtualThreadConfig {

    /**
     * 注册 TaskDecorator 后，Spring Boot 自动装配的虚拟线程执行器会自动使用它
     * 完成主线程到虚拟线程的上下文传递 (如 Sa-Token 的 ThreadLocal 数据)
     */
    @Bean
    public TaskDecorator contextCopyingDecorator() {
        return runnable -> {
            RequestAttributes context = RequestContextHolder.getRequestAttributes();
            return () -> {
                try {
                    if (context != null) {
                        RequestContextHolder.setRequestAttributes(context);
                    }
                    runnable.run();
                } finally {
                    RequestContextHolder.resetRequestAttributes();
                }
            };
        };
    }

}