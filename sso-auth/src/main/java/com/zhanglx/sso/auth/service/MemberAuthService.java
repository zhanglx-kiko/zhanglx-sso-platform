package com.zhanglx.sso.auth.service;

import com.zhanglx.sso.auth.domain.dto.MemberForgotPasswordDTO;
import com.zhanglx.sso.auth.domain.dto.MemberLoginDTO;
import com.zhanglx.sso.auth.domain.dto.MemberRegisterDTO;
import com.zhanglx.sso.auth.domain.dto.MemberVerificationCodeSendDTO;
import com.zhanglx.sso.auth.domain.dto.UserPasswordDTO;
import com.zhanglx.sso.auth.domain.vo.LoginVO;

public interface MemberAuthService {

    LoginVO login(MemberLoginDTO memberLoginDTO);

    LoginVO register(MemberRegisterDTO memberRegisterDTO);

    void sendVerificationCode(MemberVerificationCodeSendDTO sendDTO, Long memberId);

    void updatePassword(UserPasswordDTO passwordDTO);

    void forgotPassword(MemberForgotPasswordDTO forgotPasswordDTO);
}
