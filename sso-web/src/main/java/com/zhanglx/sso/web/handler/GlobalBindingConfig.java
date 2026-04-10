package com.zhanglx.sso.web.handler;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * 作者：Zhang L X
 * 创建时间：2026/3/19 17:21
 * 类名：全局绑定配置
 * 说明：处理@RequestParam / @ModelAttribute 中的字符串 去空格
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
        // - new StringTrimmerEditor(false): 仅去空格，空字符串转为 ""
        // - new StringTrimmerEditor(true): 去空格后，如果是空字符串则转换为 null
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(false));
    }
}