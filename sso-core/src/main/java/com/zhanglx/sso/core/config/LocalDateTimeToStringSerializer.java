package com.zhanglx.sso.core.config;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 将 LocalDateTime 序列化为统一的字符串格式。
 */
public class LocalDateTimeToStringSerializer extends ValueSerializer<LocalDateTime> {

    private final DateTimeFormatter formatter;

    public LocalDateTimeToStringSerializer(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public void serialize(LocalDateTime value, JsonGenerator g, SerializationContext ctxt) {
        if (value == null) {
            g.writeNull();
            return;
        }
        g.writeString(value.format(formatter));
    }

}
