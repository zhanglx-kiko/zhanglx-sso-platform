package com.zhanglx.sso.core.config;

import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.core.exception.CoreErrorCode;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/**
 * 字符串转 Long 反序列化器。
 */
public class StringToLongDeserializer extends ValueDeserializer<Long> {

    @Override
    public Long deserialize(JsonParser p, DeserializationContext ctxt) {
        JsonToken currentToken = p.currentToken();
        if (currentToken == JsonToken.VALUE_NUMBER_INT) {
            return p.getLongValue();
        }

        String value = p.getValueAsString();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw BusinessException.of(CoreErrorCode.LONG_PARSE_FAILED, e, value.trim());
        }
    }
}