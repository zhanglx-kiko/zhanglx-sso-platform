package com.zhanglx.sso.auth.oauth2.handler;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.oauth2.SaOAuth2Manager;
import cn.dev33.satoken.oauth2.data.model.AccessTokenModel;
import cn.dev33.satoken.oauth2.data.model.request.RequestAuthModel;
import cn.dev33.satoken.oauth2.granttype.handler.SaOAuth2GrantTypeHandlerInterface;
import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.web.utils.I18nUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/11 16:13
 * @ClassName: PhoneCodeGrantTypeHandler
 * @Description: 自定义 phone_code 授权模式处理器
 */
@Component
public class PhoneCodeGrantTypeHandler implements SaOAuth2GrantTypeHandlerInterface {

    @Override
    public String getHandlerGrantType() {
        return "phone_code";
    }

    @Override
    public AccessTokenModel getAccessToken(SaRequest req, String clientId, List<String> scopes) {
        // todo 手机号验证码登录
        // 获取前端提交的参数
        String phone = req.getParamNotNull("phone");
        String code = req.getParamNotNull("code");
        String realCode = SaManager.getSaTokenDao().get("phone_code:" + phone);

        // 1、校验验证码是否正确
        if (!code.equals(realCode)) {
            throw new BusinessException("invalid.verification.code");
        }

        // 2、校验通过，删除验证码
        SaManager.getSaTokenDao().delete("phone_code:" + phone);

        // 3、登录
        long userId = 10001; // 模拟 userId，真实项目应该根据手机号从数据库查询

        // 4、构建 ra 对象
        RequestAuthModel ra = new RequestAuthModel();
        ra.clientId = clientId;
        ra.loginId = userId;
        ra.scopes = scopes;

        // 5、生成 Access-Token
        return SaOAuth2Manager.getDataGenerate().generateAccessToken(ra, true, atm -> atm.grantType = "phone_code");
    }

}
