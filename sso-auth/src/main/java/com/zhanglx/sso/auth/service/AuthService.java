package com.zhanglx.sso.auth.service;

import com.zhanglx.sso.auth.domain.dto.ForgotPasswordDTO;
import com.zhanglx.sso.auth.domain.dto.ForgotPasswordVerificationCodeSendDTO;
import com.zhanglx.sso.auth.domain.dto.ForgotPasswordVerificationCodeVerifyDTO;
import com.zhanglx.sso.auth.domain.dto.UserLoginDTO;
import com.zhanglx.sso.auth.domain.dto.UserPasswordDTO;
import com.zhanglx.sso.auth.domain.vo.LoginVO;
import com.zhanglx.sso.auth.domain.vo.SmsVerificationCodeSendVO;

/**
 * 认证服务接口。
 */
public interface AuthService {

    LoginVO login(UserLoginDTO userLoginDTO);

    void updatePassword(UserPasswordDTO userPasswordDTO);

    void resetPassword(Long userId);

    SmsVerificationCodeSendVO sendForgotPasswordVerificationCode(ForgotPasswordVerificationCodeSendDTO sendDTO);

    void verifyForgotPasswordVerificationCode(ForgotPasswordVerificationCodeVerifyDTO verifyDTO);

    void forgotPassword(ForgotPasswordDTO forgotPasswordDTO);
}
