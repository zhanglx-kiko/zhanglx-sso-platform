package com.zhanglx.sso.auth.domain.properties;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 作者：Zhang L X
 * 创建时间：2026/3/19 14:26
 * 类名：WechatProperties
 * 说明：微信小程序配置属性。
 */
@Data
@Builder
@Configuration
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
@ConfigurationProperties(prefix = "wechat.miniapp")
public class WechatProperties {

    /**
     * 微信小程序应用标识。
     */
    private String appId;

    /**
     * 微信小程序密钥。
     */
    private String secret;

}