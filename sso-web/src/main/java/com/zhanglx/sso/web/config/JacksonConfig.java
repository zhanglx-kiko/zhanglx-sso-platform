package com.zhanglx.sso.web.config;

import com.zhanglx.sso.core.config.LocalDateTimeToStringSerializer;
import com.zhanglx.sso.core.config.StringToLocalDateTimeDeserializer;
import com.zhanglx.sso.core.config.StringToLongDeserializer;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;

/**
 * Jackson配置类。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapperBuilderCustomizer commonJsonMapperBuilderCustomizer() {
        return jsonMapperBuilder -> {
            SimpleModule commonModule = new SimpleModule();
            commonModule.addSerializer(LocalDateTime.class, new LocalDateTimeToStringSerializer(StringToLocalDateTimeDeserializer.DEFAULT_DATE_TIME_FORMATTER));
            commonModule.addDeserializer(LocalDateTime.class, new StringToLocalDateTimeDeserializer());
            commonModule.addSerializer(Long.class, ToStringSerializer.instance);
            commonModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
            commonModule.addDeserializer(Long.class, new StringToLongDeserializer());
            jsonMapperBuilder.addModule(commonModule);
        };
    }

}
