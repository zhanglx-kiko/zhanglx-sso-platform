package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.zhanglx.sso.auth.domain.dto.ForgotPasswordDTO;
import com.zhanglx.sso.auth.domain.dto.LoginDTO;
import com.zhanglx.sso.auth.domain.dto.UserPasswordDTO;
import com.zhanglx.sso.auth.domain.vo.LoginVO;
import com.zhanglx.sso.auth.service.AuthService;
import com.zhanglx.sso.auth.service.WechatAuthService;
import com.zhanglx.sso.core.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "认证与用户管理API")
@RequestMapping("/apis/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final WechatAuthService wechatAuthService;

    @Operation(summary = "登录接口")
    @PostMapping("/login")
    public LoginVO login(@RequestBody @Validated LoginDTO loginDTO) {
        return authService.login(loginDTO);
    }

    @PostMapping("/wechat/login")
    public LoginVO wechatLogin(@RequestParam String code) {
        return wechatAuthService.loginByWechatCode(code);
    }

    @Operation(summary = "注销登录")
    @PostMapping("/logout")
    @SaCheckLogin
    public void logout() {
        StpUtil.logout();
    }

    @Operation(summary = "查询当前登录状态")
    @GetMapping("/isLogin")
    public String isLogin() {
        return "当前会话是否登录: " + StpUtil.isLogin();
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
