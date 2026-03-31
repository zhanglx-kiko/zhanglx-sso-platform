package com.zhanglx.sso.web.annotation;

import java.lang.annotation.*;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/16 17:47
 * @ClassName: NoTrim
 * @Description: 标记在 String 字段上，表示该字段在反序列化或参数绑定时不自动去除两端空格
 */
@Target({ElementType.FIELD, ElementType.PARAMETER}) // 可以用在字段和参数上
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoTrim {
}
