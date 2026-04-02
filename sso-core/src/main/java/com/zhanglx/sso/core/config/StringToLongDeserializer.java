package com.zhanglx.sso.core.config;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import com.zhanglx.sso.core.exception.BusinessException;

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
            throw BusinessException.badRequest("无法将字符串 [" + value + "] 转换为 Long 类型", e);
        }
    }

}
