package com.zhanglx.sso.web.handler;

import com.zhanglx.sso.web.annotation.NoTrim;
import org.springframework.boot.jackson.JacksonComponent;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/19 17:18
 * @ClassName: StringTrimDeserializer
 * @Description: 处理@RequestBody请求当中的string 将字符串去空格
 */
@JacksonComponent
public class StringTrimDeserializer extends ValueDeserializer<String> {

    private boolean skipTrim = false;

    // 默认的无参构造函数，Jackson 注册时需要
    public StringTrimDeserializer() {
    }

    // 内部使用的带参构造函数，用于创建是否需要 trim 的实例
    private StringTrimDeserializer(boolean skipTrim) {
        this.skipTrim = skipTrim;
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        String text = p.getString();
        if (text != null) {
            return skipTrim ? text : text.strip();
        }

        return null;
    }

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JacksonException {
        // 判断当前正在解析的字段是否被 @NoTrim 标注
        if (property != null && property.getAnnotation(NoTrim.class) != null) {
            // 如果带有注解，返回一个不执行去空格逻辑的反序列化器实例
            return new StringTrimDeserializer(true);
        }

        // 否则返回正常的去空格反序列化器实例
        return new StringTrimDeserializer(false);
    }

}
