package com.zhanglx.sso.auth.oauth2.handler;

import cn.dev33.satoken.oauth2.granttype.handler.PasswordGrantTypeHandler;
import cn.dev33.satoken.oauth2.granttype.handler.model.PasswordAuthResult;
import com.zhanglx.sso.core.exception.BusinessException;
import org.springframework.stereotype.Component;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/11 16:04
 * @ClassName: CustomPasswordGrantTypeHandler
 * @Description: 自定义 Password Grant_Type 授权模式处理器认证过程
 */
@Component
public class CustomPasswordGrantTypeHandler extends PasswordGrantTypeHandler {

    @Override
    public PasswordAuthResult loginByUsernamePassword(String username, String password) {
        // todo 数据库查询用户信息进行登录认证
        if ("sa".equals(username) && "123456".equals(password)) {
            long userId = 10001;
            return new PasswordAuthResult(userId);
        } else {
            throw new BusinessException("business.user.password.error");
        }
    }

}
