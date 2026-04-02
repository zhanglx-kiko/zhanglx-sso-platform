package com.zhanglx.sso.web.config;

import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;
import com.zhanglx.sso.core.config.LocalDateTimeToStringSerializer;
import com.zhanglx.sso.core.config.StringToLongDeserializer;
import com.zhanglx.sso.core.config.StringToLocalDateTimeDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Bean
    public JsonMapperBuilderCustomizer commonJsonMapperBuilderCustomizer() {
        return jsonMapperBuilder -> {
            SimpleModule commonModule = new SimpleModule();
            commonModule.addSerializer(LocalDateTime.class, new LocalDateTimeToStringSerializer(DEFAULT_DATE_TIME_FORMATTER));
            commonModule.addDeserializer(LocalDateTime.class, new StringToLocalDateTimeDeserializer(DEFAULT_DATE_TIME_FORMATTER));
            commonModule.addSerializer(Long.class, ToStringSerializer.instance);
            commonModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
            commonModule.addDeserializer(Long.class, new StringToLongDeserializer());
            jsonMapperBuilder.addModule(commonModule);
        };
    }

}
