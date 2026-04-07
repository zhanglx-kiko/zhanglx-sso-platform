package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.zhanglx.sso.auth.annotation.RepeatSubmit;
import com.zhanglx.sso.auth.domain.dto.ForgotPasswordDTO;
import com.zhanglx.sso.auth.domain.dto.UserLoginDTO;
import com.zhanglx.sso.auth.domain.dto.UserPasswordDTO;
import com.zhanglx.sso.auth.domain.vo.LoginVO;
import com.zhanglx.sso.auth.service.AuthService;
import com.zhanglx.sso.auth.service.support.AuthOperationGuard;
import com.zhanglx.sso.auth.utils.RequestIdUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Auth API")
@RequestMapping("/apis/v1/auth/s")
public class AuthSysController {

    private final AuthService authService;
    private final AuthOperationGuard authOperationGuard;

    @Operation(summary = "Admin login")
    @PostMapping("/login")
    public LoginVO login(@RequestBody @Validated UserLoginDTO userLoginDTO) {
        return authService.login(userLoginDTO);
    }

    @Operation(summary = "Admin logout")
    @PostMapping("/logout")
    @SaCheckLogin
    public void logout() {
        StpUtil.logout();
    }

    @Operation(summary = "Update current user password")
    @PostMapping("/user/update/password")
    @RepeatSubmit
    @SaCheckLogin
    public void updatePassword(@RequestBody @Validated UserPasswordDTO passwordDTO) {
        passwordDTO.setUserId(StpUtil.getLoginIdAsLong());
        authService.updatePassword(passwordDTO);
    }

    @Operation(summary = "Reset user password")
    @PostMapping("/user/reset-password/{userId}")
    @RepeatSubmit
    @SaCheckPermission("user:reset")
    @Parameters({
            @Parameter(name = "userId", description = "User ID", required = true, in = ParameterIn.PATH)
    })
    public void resetPassword(@PathVariable String userId) {
        Long parsedUserId = RequestIdUtils.parseId(userId, "userId");
        authOperationGuard.checkResetPasswordNotSelf(parsedUserId);
        authService.resetPassword(parsedUserId);
    }

    @Operation(summary = "Forgot password")
    @PostMapping("/forgot-password")
    @RepeatSubmit
    public void forgotPassword(@RequestBody @Validated ForgotPasswordDTO forgotPasswordDTO) {
        authService.forgotPassword(forgotPasswordDTO);
    }
}
