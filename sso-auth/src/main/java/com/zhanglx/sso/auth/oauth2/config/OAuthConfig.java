package com.zhanglx.sso.auth.oauth2.config;

import cn.dev33.satoken.oauth2.config.SaOAuth2ServerConfig;
import cn.dev33.satoken.oauth2.strategy.SaOAuth2Strategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/11 17:50
 * @ClassName: OAuthConfig
 * @Description:
 */
@Component
public class OAuthConfig {

    @Autowired
    public void configOAuth2Server(SaOAuth2ServerConfig oauth2Server) {
        // 配置：未登录时返回的View
        SaOAuth2Strategy.instance.notLoginView = ()->{
            return new ModelAndView("redirect:http://localhost:5173/login");
        };
    }

}
