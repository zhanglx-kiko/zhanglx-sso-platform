package com.zhanglx.sso.core.config;

import com.zhanglx.sso.core.enums.IBaseEnum;
import com.zhanglx.sso.core.utils.enums.EnumUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.*;

/**
 * 基础枚举反序列化器。
 */
public class IBaseEnumJsonDeserializer extends ValueDeserializer<Object> {

    /**
     * 目标枚举类型。
     */
    private final Class<? extends Enum<?>> enumType;

    public IBaseEnumJsonDeserializer() {
        this(null);
    }

    /**
     * 私有构造方法，用于绑定具体的枚举类型。
     */
    private IBaseEnumJsonDeserializer(Class<? extends Enum<?>> enumType) {
        this.enumType = enumType;
    }

    @Override
    public Object deserialize(JsonParser parser, DeserializationContext context) {
        if (enumType == null) {
            return null;
        }

        JsonToken token = parser.currentToken();
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }

        Object code = switch (token) {
            case VALUE_STRING -> normalizeString(parser.getValueAsString());
            case VALUE_NUMBER_INT, VALUE_NUMBER_FLOAT -> parser.getNumberValue();
            case VALUE_TRUE, VALUE_FALSE -> parser.getBooleanValue();
            default -> resolveFromTree(parser.readValueAsTree());
        };

        if (code == null) {
            return null;
        }

        Object byName = resolveByEnumName(code);
        if (byName != null) {
            return byName;
        }

        Object byCode = EnumUtils.codeOf((Class) enumType, code);
        if (byCode != null) {
            return byCode;
        }

        throw com.zhanglx.sso.core.exception.BusinessException.badRequest("Unsupported enum code: " + code);
    }

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) throws JacksonException {
        JavaType targetType = property != null ? property.getType() : context.getContextualType();
        if (targetType == null || targetType.getRawClass() == null) {
            return this;
        }
        Class<?> rawClass = targetType.getRawClass();
        if (!rawClass.isEnum() || !IBaseEnum.class.isAssignableFrom(rawClass)) {
            return this;
        }
        return new IBaseEnumJsonDeserializer((Class<? extends Enum<?>>) rawClass);
    }

    /**
     * 解析byEnumName。
     */
    private Object resolveByEnumName(Object code) {
        if (!(code instanceof String text) || text.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf((Class) enumType, text.trim());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * 解析fromTree。
     */
    private Object resolveFromTree(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isValueNode()) {
            return node.isNumber() ? node.numberValue() : normalizeString(node.asText());
        }
        JsonNode codeNode = node.get("code");
        if (codeNode == null || codeNode.isNull()) {
            return null;
        }
        return codeNode.isNumber() ? codeNode.numberValue() : normalizeString(codeNode.asText());
    }

    /**
     * 规范化string。
     */
    private String normalizeString(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        String normalized = rawValue.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}