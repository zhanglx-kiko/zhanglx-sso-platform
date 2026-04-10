package com.zhanglx.sso.core.annotation;

import java.lang.annotation.*;

/**
 * 作者：Zhang L X
 * 创建时间：2026/3/17 17:47
 * 类名：ExportTreeJson
 * 说明：声明式树结构 JSON 流式导出注解
 * 标注在 控制器方法上，自动将返回的 List<T> 以流的形式写入 HTTP 响应
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