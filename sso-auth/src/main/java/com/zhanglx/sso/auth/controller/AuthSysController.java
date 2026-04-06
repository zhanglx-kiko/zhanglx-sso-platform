package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.zhanglx.sso.auth.domain.dto.ForgotPasswordDTO;
import com.zhanglx.sso.auth.domain.dto.UserLoginDTO;
import com.zhanglx.sso.auth.domain.dto.UserPasswordDTO;
import com.zhanglx.sso.auth.domain.vo.LoginVO;
import com.zhanglx.sso.auth.service.AuthService;
import com.zhanglx.sso.core.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "认证与用户管理API")
@RequestMapping("/apis/v1/auth/s")
public class AuthSysController {

    private final AuthService authService;

    @Operation(summary = "【B端】后台管理登录")
    @PostMapping("/login")
    public LoginVO login(@RequestBody @Validated UserLoginDTO userLoginDTO) {
        return authService.login(userLoginDTO);
    }

    @Operation(summary = "【B端】后台管理登出")
    @PostMapping("/logout")
    @SaCheckLogin
    public void logout() {
        StpUtil.logout();
    }

    @Operation(summary = "修改用户密码")
    @PostMapping("/user/update/password")
    @SaCheckLogin
    public void updatePassword(@RequestBody @Validated UserPasswordDTO passwordDTO) {
        passwordDTO.setUserId(StpUtil.getLoginIdAsLong());
        authService.updatePassword(passwordDTO);
    }

    @Operation(summary = "管理员重置密码")
    @PostMapping("/user/reset-password/{userId}")
    @SaCheckPermission("user:reset")
    @Parameters({
            @Parameter(name = "userId", description = "用户id", required = true, in = ParameterIn.PATH)
    })
    public void resetPassword(@PathVariable String userId) {
        authService.resetPassword(parseUserId(userId));
    }

    @Operation(summary = "忘记密码")
    @PostMapping("/forgot-password")
    public void forgotPassword(@RequestBody @Validated ForgotPasswordDTO forgotPasswordDTO) {
        authService.forgotPassword(forgotPasswordDTO);
    }

    private Long parseUserId(String userId) {
        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException e) {
            throw BusinessException.badRequest("无效的用户ID: " + userId, e);
        }
    }

}
