package com.zhanglx.sso.auth.service;

import com.zhanglx.sso.auth.domain.vo.LoginVO;

/**
 * WechatAuth服务接口。
 */
public interface WechatAuthService {

    LoginVO loginByWechatCode(String code);

    LoginVO loginMemberByWechatCode(String code);
}