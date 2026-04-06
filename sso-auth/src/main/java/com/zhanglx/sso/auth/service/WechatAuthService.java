package com.zhanglx.sso.auth.service;

import com.zhanglx.sso.auth.domain.vo.LoginVO;

public interface WechatAuthService {

    LoginVO loginByWechatCode(String code);

    LoginVO loginMemberByWechatCode(String code);
}
