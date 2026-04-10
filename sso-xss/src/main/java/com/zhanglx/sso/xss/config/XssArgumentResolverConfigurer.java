package com.zhanglx.sso.xss.config;

import com.zhanglx.sso.xss.resolver.XssMultipartStringPartResolver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 把 multipart 文本分片解析器前插到参数解析链。
 * 这里不用依赖默认解析器顺序，能稳定覆盖 @RequestPart String 这类文本分片场景。
 */
@Component
public class XssArgumentResolverConfigurer implements BeanPostProcessor {

    private final ObjectProvider<XssMultipartStringPartResolver> resolverProvider;

    public XssArgumentResolverConfigurer(ObjectProvider<XssMultipartStringPartResolver> resolverProvider) {
        this.resolverProvider = resolverProvider;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!(bean instanceof RequestMappingHandlerAdapter adapter)) {
            return bean;
        }
        XssMultipartStringPartResolver xssMultipartStringPartResolver = resolverProvider.getIfAvailable();
        if (xssMultipartStringPartResolver == null) {
            return bean;
        }
        List<HandlerMethodArgumentResolver> currentResolvers = adapter.getArgumentResolvers();
        if (currentResolvers == null || currentResolvers.stream().anyMatch(xssMultipartStringPartResolver::equals)) {
            return bean;
        }
        List<HandlerMethodArgumentResolver> mergedResolvers = new ArrayList<>(currentResolvers.size() + 1);
        mergedResolvers.add(xssMultipartStringPartResolver);
        mergedResolvers.addAll(currentResolvers);
        adapter.setArgumentResolvers(mergedResolvers);
        return bean;
    }
}