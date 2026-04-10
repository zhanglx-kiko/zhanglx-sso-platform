package com.zhanglx.sso.auth.domain.properties;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/19 14:26
 * @ClassName: WechatProperties
 * @Description: 微信小程序配置属性。
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
