package com.zhanglx.sso.core.config;

import com.zhanglx.sso.core.exception.BusinessException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 将前端传入的字符串反序列化为 LocalDateTime。
 */
public class StringToLocalDateTimeDeserializer extends ValueDeserializer<LocalDateTime> {

    private final DateTimeFormatter formatter;

    public StringToLocalDateTimeDeserializer(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) {
        String value = p.getValueAsString();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(value.trim(), formatter);
        } catch (DateTimeParseException e) {
            throw BusinessException.badRequest("无法将字符串 [" + value + "] 转换为 LocalDateTime 类型", e);
        }
    }

}
