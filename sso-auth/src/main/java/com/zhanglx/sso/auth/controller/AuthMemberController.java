package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.zhanglx.sso.auth.domain.dto.MemberForgotPasswordDTO;
import com.zhanglx.sso.auth.domain.dto.MemberLoginDTO;
import com.zhanglx.sso.auth.domain.dto.MemberRegisterDTO;
import com.zhanglx.sso.auth.domain.dto.MemberVerificationCodeSendDTO;
import com.zhanglx.sso.auth.domain.dto.UserPasswordDTO;
import com.zhanglx.sso.auth.domain.vo.LoginVO;
import com.zhanglx.sso.auth.service.MemberAuthService;
import com.zhanglx.sso.auth.service.WechatAuthService;
import com.zhanglx.sso.core.utils.satoken.StpMemberUtil;
import com.zhanglx.sso.web.annotation.RateLimitDimension;
import com.zhanglx.sso.web.annotation.RepeatSubmit;
import com.zhanglx.sso.web.annotation.RequestRateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "C端认证接口")
@RequestMapping("/apis/v1/auth/m")
public class AuthMemberController {

    private final MemberAuthService memberAuthService;
    private final WechatAuthService wechatAuthService;

    @Operation(summary = "会员账号密码登录")
    @PostMapping("/login")
    @RequestRateLimit(limit = 5, windowSeconds = 60, dimensions = {RateLimitDimension.IP, RateLimitDimension.URI}, customKey = "#memberLoginDTO.phoneNumber")
    public LoginVO memberLogin(@RequestBody @Validated MemberLoginDTO memberLoginDTO) {
        return memberAuthService.login(memberLoginDTO);
    }

    @Operation(summary = "会员注册")
    @PostMapping("/register")
    @RepeatSubmit
    @RequestRateLimit(limit = 3, windowSeconds = 300, dimensions = {RateLimitDimension.IP, RateLimitDimension.URI}, customKey = "#memberRegisterDTO.phoneNumber")
    public LoginVO register(@RequestBody @Validated MemberRegisterDTO memberRegisterDTO) {
        return memberAuthService.register(memberRegisterDTO);
    }

    @Operation(summary = "发送会员验证码")
    @PostMapping("/verification-code/send")
    @RequestRateLimit(limit = 3, windowSeconds = 300, dimensions = {RateLimitDimension.IP, RateLimitDimension.URI}, customKey = "#sendDTO.phoneNumber + ':' + #sendDTO.scene")
    public void sendVerificationCode(@RequestBody @Validated MemberVerificationCodeSendDTO sendDTO) {
        Long memberId = StpMemberUtil.isLogin() ? StpMemberUtil.getLoginIdAsLong() : null;
        memberAuthService.sendVerificationCode(sendDTO, memberId);
    }

    @Operation(summary = "会员微信登录")
    @PostMapping("/wechat/login")
    @RepeatSubmit
    @RequestRateLimit(limit = 5, windowSeconds = 60, dimensions = {RateLimitDimension.IP, RateLimitDimension.URI}, customKey = "#code")
    public LoginVO wechatLogin(@RequestParam String code) {
        return wechatAuthService.loginMemberByWechatCode(code);
    }

    @Operation(summary = "会员登出")
    @PostMapping("/logout")
    @SaCheckLogin(type = StpMemberUtil.TYPE)
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    public void memberLogout() {
        StpMemberUtil.logout();
    }

    @Operation(summary = "会员修改密码")
    @PostMapping("/user/update/password")
    @RepeatSubmit
    @SaCheckLogin(type = StpMemberUtil.TYPE)
    @RequestRateLimit(limit = 3, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    public void updatePassword(@RequestBody @Validated UserPasswordDTO passwordDTO) {
        passwordDTO.setUserId(StpMemberUtil.getLoginIdAsLong());
        memberAuthService.updatePassword(passwordDTO);
    }

    @Operation(summary = "会员忘记密码")
    @PostMapping("/forgot-password")
    @RepeatSubmit
    @RequestRateLimit(limit = 3, windowSeconds = 300, dimensions = {RateLimitDimension.IP, RateLimitDimension.URI}, customKey = "#forgotPasswordDTO.phoneNumber")
    public void forgotPassword(@RequestBody @Validated MemberForgotPasswordDTO forgotPasswordDTO) {
        memberAuthService.forgotPassword(forgotPasswordDTO);
    }
}
