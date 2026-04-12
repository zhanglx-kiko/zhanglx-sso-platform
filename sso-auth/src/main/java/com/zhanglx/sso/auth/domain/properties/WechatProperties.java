package com.zhanglx.sso.auth.domain.properties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 作者：Zhang L X
 * 创建时间：2026/3/19 14:26
 * 类名：WechatProperties
 * 说明：微信小程序配置属性。
 */
@Data
@Builder
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
@Deprecated(forRemoval = false)
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
