package com.zhanglx.sso.auth.oauth2.controller;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.oauth2.SaOAuth2Manager;
import cn.dev33.satoken.oauth2.processor.SaOAuth2ServerProcessor;
import cn.dev33.satoken.oauth2.template.SaOAuth2Util;
import cn.dev33.satoken.util.SaFoxUtil;
import com.zhanglx.sso.auth.domain.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/11 16:11
 * @ClassName: SOAuth2Controller
 * @Description:
 */
@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class SOAuth2Controller {

    // 获取 userinfo 信息：昵称、头像、性别等等
    @RequestMapping("/userinfo")
    public UserDTO userinfo() {
        // 获取 Access-Token 对应的账号id
        String accessToken = SaOAuth2Manager.getDataResolver().readAccessToken(SaHolder.getRequest());
        Object loginId = SaOAuth2Util.getLoginIdByAccessToken(accessToken);
        System.out.println("-------- 此Access-Token对应的账号id: " + loginId);

        // 校验 Access-Token 是否具有权限: userinfo
        SaOAuth2Util.checkAccessTokenScope(accessToken, "userinfo");

        // 模拟账号信息 （真实环境需要查询数据库获取信息）
        // todo 从数据库查询用户信息
        return new UserDTO();
    }

    @RequestMapping("/sendPhoneCode")
    public void sendCode(String phone) {
        // todo 验证码登录
        String code = SaFoxUtil.getRandomNumber(100000, 999999) + "";
        SaManager.getSaTokenDao().set("phone_code:" + phone, code, 60 * 5);
        System.out.println("手机号：" + phone + "，验证码：" + code + "，已发送成功");
    }

    // 模式一：Code授权码 || 模式二：隐藏式
    @RequestMapping("/authorize")
    public Object authorize() {
        return SaOAuth2ServerProcessor.instance.authorize();
    }

    // 用户登录
    /*@RequestMapping("/doLogin")
    public Object doLogin() {
        return SaOAuth2ServerProcessor.instance.doLogin();
    }*/

    // 用户确认授权
    @RequestMapping("/doConfirm")
    public Object doConfirm() {
        return SaOAuth2ServerProcessor.instance.doConfirm();
    }

    // Code 换 Access-Token || 模式三：密码式
    @RequestMapping("/token")
    public Object token() {
        return SaOAuth2ServerProcessor.instance.token();
    }

    // Refresh-Token 刷新 Access-Token
    @RequestMapping("/refresh")
    public Object refresh() {
        return SaOAuth2ServerProcessor.instance.refresh();
    }

    // 回收 Access-Token
    @RequestMapping("/revoke")
    public Object revoke() {
        return SaOAuth2ServerProcessor.instance.revoke();
    }

    // 模式四：凭证式
    @RequestMapping("/client_token")
    public Object clientToken() {
        return SaOAuth2ServerProcessor.instance.clientToken();
    }

}
