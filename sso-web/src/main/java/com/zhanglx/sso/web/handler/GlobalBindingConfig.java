package com.zhanglx.sso.web.handler;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/19 17:21
 * @ClassName: GlobalBindingConfig
 * @Description: 处理@RequestParam / @ModelAttribute 中的字符串 去空格
 */
@ControllerAdvice
public class GlobalBindingConfig {

    /**
     * 对表单参数、URL参数进行处理
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // 第一个参数是目标类型
        // 第二个参数是具体的 Editor：
        //   - new StringTrimmerEditor(false): 仅去空格，空字符串转为 ""
        //   - new StringTrimmerEditor(true): 去空格后，如果是空字符串则转换为 null
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(false));
    }

    // 针对特定的 Controller 或通过参数名来特殊处理
    // 例如：如果参数名叫做 password 或者 rawData，则不去除空格
    @InitBinder
    public void initBinderSpecificFields(WebDataBinder binder) {
        // 注意：这里注册自定义编辑器时，未指定目标类型，这在 Spring 内部有时会导致问题，不推荐。
        // 更稳妥的方式是：
    }

}
