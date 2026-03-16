package com.zhanglx.sso.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/12 17:04
 * @ClassName: JacksonConfig
 * @Description: Jackson 序列化配置，处理 Long 类型转 String（只针对标注了注解的字段）
 */
@Configuration
public class JacksonConfig implements WebMvcConfigurer {

    /**
     * 默认时间格式：yyyy-MM-dd HH:mm:ss
     */
    private static final String DEFAULT_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 创建 JavaTimeModule 并配置时间格式
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // 配置 LocalDateTime 的序列化器和反序列化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_PATTERN);
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));

        objectMapper.registerModule(javaTimeModule);

        // 创建自定义模块
        SimpleModule simpleModule = new SimpleModule();

        // 注册 Long → String 序列化器
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);

        // 注册自定义模块
        objectMapper.registerModule(simpleModule);

        return objectMapper;
    }

    @Override
    @SuppressWarnings("removal")
    public void configureMessageConverters(List<org.springframework.http.converter.HttpMessageConverter<?>> converters) {
        // 添加 MappingJackson2HttpMessageConverter，使用自定义的 ObjectMapper
        MappingJackson2HttpMessageConverter jackson2HttpMessageConverter =
                new MappingJackson2HttpMessageConverter(objectMapper());

        // 添加到列表末尾
        converters.add(jackson2HttpMessageConverter);
    }

}
