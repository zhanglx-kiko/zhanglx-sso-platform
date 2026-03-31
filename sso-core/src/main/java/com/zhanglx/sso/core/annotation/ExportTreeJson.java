package com.zhanglx.sso.core.annotation;

import java.lang.annotation.*;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/17 17:47
 * @ClassName: ExportTreeJson
 * @Description: 声明式树结构 JSON 流式导出注解
 * 标注在 Controller 方法上，自动将返回的 List<T> 以流的形式写入 HTTP 响应
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExportTreeJson {

    /**
     * 导出的文件名（不含扩展名），默认取 tree_data
     */
    String fileName() default "json_data";

}
