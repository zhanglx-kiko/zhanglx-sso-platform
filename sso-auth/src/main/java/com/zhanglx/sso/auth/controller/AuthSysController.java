package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.zhanglx.sso.auth.domain.dto.ForgotPasswordDTO;
import com.zhanglx.sso.auth.domain.dto.UserLoginDTO;
import com.zhanglx.sso.auth.domain.dto.UserPasswordDTO;
import com.zhanglx.sso.auth.domain.vo.LoginVO;
import com.zhanglx.sso.auth.service.AuthService;
import com.zhanglx.sso.auth.service.support.AuthOperationGuard;
import com.zhanglx.sso.auth.utils.RequestIdUtils;
import com.zhanglx.sso.web.annotation.RateLimitDimension;
import com.zhanglx.sso.web.annotation.RepeatSubmit;
import com.zhanglx.sso.web.annotation.RequestRateLimit;
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
    @RequestRateLimit(limit = 5, windowSeconds = 60, dimensions = {RateLimitDimension.IP, RateLimitDimension.URI}, customKey = "#userLoginDTO.username")
    public LoginVO login(@RequestBody @Validated UserLoginDTO userLoginDTO) {
        return authService.login(userLoginDTO);
    }

    @Operation(summary = "Admin logout")
    @PostMapping("/logout")
    @SaCheckLogin
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    public void logout() {
        StpUtil.logout();
    }

    @Operation(summary = "Update current user password")
    @PostMapping("/user/update/password")
    @RepeatSubmit
    @SaCheckLogin
    @RequestRateLimit(limit = 3, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    public void updatePassword(@RequestBody @Validated UserPasswordDTO passwordDTO) {
        passwordDTO.setUserId(StpUtil.getLoginIdAsLong());
        authService.updatePassword(passwordDTO);
    }

    @Operation(summary = "Reset user password")
    @PostMapping("/user/reset-password/{userId}")
    @RepeatSubmit
    @SaCheckPermission("user:reset")
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
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
    @RequestRateLimit(limit = 3, windowSeconds = 300, dimensions = {RateLimitDimension.IP, RateLimitDimension.URI})
    public void forgotPassword(@RequestBody @Validated ForgotPasswordDTO forgotPasswordDTO) {
        authService.forgotPassword(forgotPasswordDTO);
    }
}
