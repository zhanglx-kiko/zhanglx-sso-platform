package com.zhanglx.sso.auth.service;

import com.zhanglx.sso.auth.domain.dto.ForgotPasswordDTO;
import com.zhanglx.sso.auth.domain.dto.UserLoginDTO;
import com.zhanglx.sso.auth.domain.dto.UserPasswordDTO;
import com.zhanglx.sso.auth.domain.vo.LoginVO;

public interface AuthService {

    LoginVO login(UserLoginDTO userLoginDTO);

    void updatePassword(UserPasswordDTO userPasswordDTO);

    void resetPassword(Long userId);

    void forgotPassword(ForgotPasswordDTO forgotPasswordDTO);
}
