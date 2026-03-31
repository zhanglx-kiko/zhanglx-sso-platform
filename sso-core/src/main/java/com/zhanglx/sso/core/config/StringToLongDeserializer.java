package com.zhanglx.sso.core.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.zhanglx.sso.core.exception.BusinessException;

import java.io.IOException;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/12 15:34
 * @ClassName: StringToLongDeserializer
 * @Description: String 转 Long 自定义反序列化器
 */
public class StringToLongDeserializer extends JsonDeserializer<Long> {

    @Override
    public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new BusinessException("无法将字符串 [" + value + "] 转换为 Long 类型", e.getMessage());
        }
    }

}
