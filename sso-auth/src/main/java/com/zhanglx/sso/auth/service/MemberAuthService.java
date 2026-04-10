package com.zhanglx.sso.auth.service;

import com.zhanglx.sso.auth.domain.dto.*;
import com.zhanglx.sso.auth.domain.vo.LoginVO;

/**
 * MemberAuth服务接口。
 */
public interface MemberAuthService {

    LoginVO login(MemberLoginDTO memberLoginDTO);

    LoginVO register(MemberRegisterDTO memberRegisterDTO);

    void sendVerificationCode(MemberVerificationCodeSendDTO sendDTO, Long memberId);

    void updatePassword(UserPasswordDTO passwordDTO);

    void forgotPassword(MemberForgotPasswordDTO forgotPasswordDTO);
}