package com.zhanglx.sso.auth.service;

import com.zhanglx.sso.auth.domain.vo.LoginVO;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/19 14:27
 * @ClassName: WechatAuthService
 * @Description: 微信授权服务接口
 * <p>
 * 主要职责：
 * 1. 处理微信小程序授权登录
 * 2. 微信 OpenID 与系统用户的关联
 */
public interface WechatAuthService {

    /**
     * 微信授权登录
     * <p>
     * 业务逻辑：
     * 1. 使用 code 调用微信接口换取 openid
     * 2. 根据 openid 查询系统用户
     * 3. 如果未关联，创建新用户并绑定
     * 4. 执行 Sa-Token 登录
     * 5. 返回 Token
     *
     * @param code 微信小程序授权码（由 wx.login() 获取）
     * @return LoginVO 登录结果（用户信息 + Token）
     */
    LoginVO loginByWechatCode(String code);

}
