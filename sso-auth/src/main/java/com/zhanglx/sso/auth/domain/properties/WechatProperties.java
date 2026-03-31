package com.zhanglx.sso.auth.domain.properties;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/19 14:26
 * @ClassName: WechatProperties
 * @Description:
 */
@Data
@Builder
@Configuration
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
@ConfigurationProperties(prefix = "wechat.miniapp")
public class WechatProperties {

    private String appId;
    private String secret;

}
