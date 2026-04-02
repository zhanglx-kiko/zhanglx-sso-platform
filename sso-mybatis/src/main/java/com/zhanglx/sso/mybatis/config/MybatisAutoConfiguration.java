package com.zhanglx.sso.mybatis.config;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.zhanglx.sso.mybatis.handler.AutoEnumTypeHandler;
import com.zhanglx.sso.mybatis.handler.DefaultDBFieldHandler;
import com.zhanglx.sso.mybatis.handler.StringListTypeHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/5 16:17
 * @ClassName: MybatisAutoConfiguration
 * @Description:
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
public class MybatisAutoConfiguration {

    /**
     * 字段自动填充
     *
     * @return
     */
    @Bean
    public MetaObjectHandler defaultDBFieldHandler() {
        return new DefaultDBFieldHandler();
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor()); // 分页插件
        mybatisPlusInterceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return mybatisPlusInterceptor;
    }

    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> {
            //此处注册对查询条件有用，否则查询条件不会经过typehandler的处理
            configuration.getTypeHandlerRegistry().setDefaultEnumTypeHandler(AutoEnumTypeHandler.class);
            configuration.getTypeHandlerRegistry().register(List.class, StringListTypeHandler.class);
        };
    }

}
