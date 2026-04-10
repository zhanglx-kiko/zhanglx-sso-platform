package com.zhanglx.sso.core.config;

import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.core.exception.CoreErrorCode;
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

    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DateTimeFormatter formatter;

    public StringToLocalDateTimeDeserializer() {
        this(DEFAULT_DATE_TIME_FORMATTER);
    }

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
            throw BusinessException.of(CoreErrorCode.LOCAL_DATE_TIME_PARSE_FAILED, e, value.trim());
        }
    }
}