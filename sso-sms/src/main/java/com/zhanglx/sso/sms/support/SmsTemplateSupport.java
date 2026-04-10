package com.zhanglx.sso.sms.support;

import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.sms.enums.SmsSceneType;
import com.zhanglx.sso.sms.properties.SmsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class SmsTemplateSupport {

    private final SmsProperties smsProperties;

    public SmsProperties.TemplateProperties getTemplate(SmsSceneType sceneType) {
        SmsProperties.TemplateProperties template = smsProperties.getTemplates().get(sceneType.getCode());
        if (template == null || !StringUtils.hasText(template.getTemplateCode())) {
            throw BusinessException.internalError("technical.sms.template.not.configured");
        }
        return template;
    }

    public String renderSmsChineseContent(SmsProperties.TemplateProperties template, Map<String, String> templateParams) {
        if (template == null || !StringUtils.hasText(template.getSmsChineseContent())) {
            throw BusinessException.internalError("technical.sms.template.not.configured");
        }
        if (templateParams == null || templateParams.isEmpty()) {
            throw BusinessException.internalError("technical.sms.template.param.invalid");
        }

        String rendered = template.getSmsChineseContent();
        for (Map.Entry<String, String> entry : templateParams.entrySet()) {
            rendered = rendered.replace("${" + entry.getKey() + "}", entry.getValue());
        }

        if (rendered.contains("${")) {
            throw BusinessException.internalError("technical.sms.template.param.invalid");
        }
        return rendered;
    }
}