package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhanglx.sso.auth.domain.dto.AdminMemberForceLogoutDTO;
import com.zhanglx.sso.auth.domain.dto.AdminMemberStatusUpdateDTO;
import com.zhanglx.sso.auth.domain.dto.MemberForgotPasswordDTO;
import com.zhanglx.sso.auth.domain.dto.MemberLoginDTO;
import com.zhanglx.sso.auth.domain.dto.MemberRegisterDTO;
import com.zhanglx.sso.auth.domain.dto.MemberVerificationCodeSendDTO;
import com.zhanglx.sso.auth.domain.dto.UserPasswordDTO;
import com.zhanglx.sso.auth.domain.vo.LoginVO;
import com.zhanglx.sso.auth.service.AdminMemberManageService;
import com.zhanglx.sso.auth.service.MemberAuthService;
import com.zhanglx.sso.auth.service.WechatAuthService;
import com.zhanglx.sso.auth.service.support.AuthLoginAuditSupport;
import com.zhanglx.sso.auth.utils.RequestIdUtils;
import com.zhanglx.sso.core.utils.satoken.StpMemberUtil;
import com.zhanglx.sso.log.annotation.OperationLog;
import com.zhanglx.sso.web.annotation.RateLimitDimension;
import com.zhanglx.sso.web.annotation.RepeatSubmit;
import com.zhanglx.sso.web.annotation.RequestRateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证会员控制器。
 */
@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "C端会员认证接口")
public class AuthMemberController {

    /**
     * 会员认证服务。
     */
    private final MemberAuthService memberAuthService;

    /**
     * 微信认证服务。
     */
    private final WechatAuthService wechatAuthService;

    /**
     * 登录审计支持组件。
     */
    private final AuthLoginAuditSupport authLoginAuditSupport;

    /**
     * 会员后台管理服务。
     */
    private final AdminMemberManageService adminMemberManageService;

    @Operation(summary = "会员账号密码登录")
    @PostMapping("/apis/v1/auth/m/login")
    @RequestRateLimit(limit = 5, windowSeconds = 60, dimensions = {RateLimitDimension.IP, RateLimitDimension.URI}, customKey = "#memberLoginDTO.phoneNumber")
    public LoginVO memberLogin(@RequestBody @Validated MemberLoginDTO memberLoginDTO) {
        try {
            LoginVO loginVO = memberAuthService.login(memberLoginDTO);
            authLoginAuditSupport.recordLoginSuccess(
                    loginVO.getId(),
                    loginVO.getUsername(),
                    loginVO.getNickname(),
                    memberLoginDTO.getDevice(),
                    AuthLoginAuditSupport.CLIENT_TYPE_MEMBER_PASSWORD
            );
            return loginVO;
        } catch (Exception e) {
            authLoginAuditSupport.recordLoginFailure(
                    memberLoginDTO.getPhoneNumber(),
                    memberLoginDTO.getPhoneNumber(),
                    memberLoginDTO.getDevice(),
                    AuthLoginAuditSupport.CLIENT_TYPE_MEMBER_PASSWORD,
                    e
            );
            throw e;
        }
    }

    @Operation(summary = "会员注册")
    @PostMapping("/apis/v1/auth/m/register")
    @RepeatSubmit
    @RequestRateLimit(limit = 3, windowSeconds = 300, dimensions = {RateLimitDimension.IP, RateLimitDimension.URI}, customKey = "#memberRegisterDTO.phoneNumber")
    public LoginVO register(@RequestBody @Validated MemberRegisterDTO memberRegisterDTO) {
        LoginVO loginVO = memberAuthService.register(memberRegisterDTO);
        authLoginAuditSupport.recordLoginSuccess(
                loginVO.getId(),
                loginVO.getUsername(),
                loginVO.getNickname(),
                memberRegisterDTO.getDevice(),
                AuthLoginAuditSupport.CLIENT_TYPE_MEMBER_PASSWORD
        );
        return loginVO;
    }

    @Operation(summary = "发送会员验证码")
    @PostMapping("/apis/v1/auth/m/verification-code/send")
    @RequestRateLimit(limit = 3, windowSeconds = 300, dimensions = {RateLimitDimension.IP, RateLimitDimension.URI}, customKey = "#sendDTO.phoneNumber + ':' + #sendDTO.scene")
    public void sendVerificationCode(@RequestBody @Validated MemberVerificationCodeSendDTO sendDTO) {
        memberAuthService.sendVerificationCode(sendDTO, resolveCurrentMemberIdSafely());
    }

    @Operation(summary = "会员微信登录")
    @PostMapping("/apis/v1/auth/m/wechat/login")
    @RepeatSubmit
    @RequestRateLimit(limit = 5, windowSeconds = 60, dimensions = {RateLimitDimension.IP, RateLimitDimension.URI}, customKey = "#code")
    public LoginVO wechatLogin(@RequestParam String code) {
        try {
            LoginVO loginVO = wechatAuthService.loginMemberByWechatCode(code);
            authLoginAuditSupport.recordLoginSuccess(
                    loginVO.getId(),
                    loginVO.getUsername(),
                    loginVO.getNickname(),
                    null,
                    AuthLoginAuditSupport.CLIENT_TYPE_MEMBER_WECHAT
            );
            return loginVO;
        } catch (Exception e) {
            authLoginAuditSupport.recordLoginFailure(
                    "wechat",
                    "wechat",
                    null,
                    AuthLoginAuditSupport.CLIENT_TYPE_MEMBER_WECHAT,
                    e
            );
            throw e;
        }
    }

    @Operation(summary = "会员登出")
    @PostMapping("/apis/v1/auth/m/logout")
    @SaCheckLogin(type = StpMemberUtil.TYPE)
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    public void memberLogout() {
        AuthLoginAuditSupport.SessionSnapshot snapshot = authLoginAuditSupport.currentMemberSnapshot();
        StpMemberUtil.logout();
        authLoginAuditSupport.recordLogout(
                snapshot.userId(),
                snapshot.username(),
                snapshot.displayName(),
                null,
                snapshot.clientType()
        );
    }

    @Operation(summary = "会员修改密码")
    @PostMapping("/apis/v1/auth/m/user/update/password")
    @RepeatSubmit
    @SaCheckLogin(type = StpMemberUtil.TYPE)
    @RequestRateLimit(limit = 3, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @OperationLog(module = "会员认证", feature = "密码", operationType = "UPDATE", operationName = "修改会员密码", operationDesc = "会员修改自己的登录密码", includeRequestBody = false, includeResponseBody = false)
    public void updatePassword(@RequestBody @Validated UserPasswordDTO passwordDTO) {
        passwordDTO.setUserId(StpMemberUtil.getLoginIdAsLong());
        memberAuthService.updatePassword(passwordDTO);
    }

    @Operation(summary = "会员忘记密码")
    @PostMapping("/apis/v1/auth/m/forgot-password")
    @RepeatSubmit
    @RequestRateLimit(limit = 3, windowSeconds = 300, dimensions = {RateLimitDimension.IP, RateLimitDimension.URI}, customKey = "#forgotPasswordDTO.phoneNumber")
    @OperationLog(module = "会员认证", feature = "密码", operationType = "RESET", operationName = "会员忘记密码", operationDesc = "会员通过忘记密码流程重置密码", includeRequestBody = false, includeResponseBody = false)
    public void forgotPassword(@RequestBody @Validated MemberForgotPasswordDTO forgotPasswordDTO) {
        memberAuthService.forgotPassword(forgotPasswordDTO);
    }

    @Operation(summary = "后台禁用会员")
    @PatchMapping("/apis/v1/auth/s/members/{memberId}/disable")
    @RepeatSubmit
    @SaCheckPermission("member:disable")
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @OperationLog(module = "会员管理", feature = "会员状态", operationType = "DISABLE", operationName = "禁用会员", operationDesc = "后台禁用会员并清理会员登录态", includeResponseBody = false)
    public void disableMember(@PathVariable String memberId, @RequestBody @Valid AdminMemberStatusUpdateDTO dto) {
        dto.setMemberId(RequestIdUtils.parseId(memberId, "memberId"));
        adminMemberManageService.disable(dto);
    }

    @Operation(summary = "后台启用会员")
    @PatchMapping("/apis/v1/auth/s/members/{memberId}/enable")
    @RepeatSubmit
    @SaCheckPermission("member:enable")
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @OperationLog(module = "会员管理", feature = "会员状态", operationType = "ENABLE", operationName = "启用会员", operationDesc = "后台启用会员", includeResponseBody = false)
    public void enableMember(@PathVariable String memberId, @RequestBody @Valid AdminMemberStatusUpdateDTO dto) {
        dto.setMemberId(RequestIdUtils.parseId(memberId, "memberId"));
        adminMemberManageService.enable(dto);
    }

    @Operation(summary = "后台冻结会员")
    @PatchMapping("/apis/v1/auth/s/members/{memberId}/freeze")
    @RepeatSubmit
    @SaCheckPermission("member:freeze")
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @OperationLog(module = "会员管理", feature = "会员状态", operationType = "FREEZE", operationName = "冻结会员", operationDesc = "后台冻结会员并清理会员登录态", includeResponseBody = false)
    public void freezeMember(@PathVariable String memberId, @RequestBody @Valid AdminMemberStatusUpdateDTO dto) {
        dto.setMemberId(RequestIdUtils.parseId(memberId, "memberId"));
        adminMemberManageService.freeze(dto);
    }

    @Operation(summary = "后台解冻会员")
    @PatchMapping("/apis/v1/auth/s/members/{memberId}/unfreeze")
    @RepeatSubmit
    @SaCheckPermission("member:unfreeze")
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @OperationLog(module = "会员管理", feature = "会员状态", operationType = "UNFREEZE", operationName = "解冻会员", operationDesc = "后台解冻会员", includeResponseBody = false)
    public void unfreezeMember(@PathVariable String memberId, @RequestBody @Valid AdminMemberStatusUpdateDTO dto) {
        dto.setMemberId(RequestIdUtils.parseId(memberId, "memberId"));
        adminMemberManageService.unfreeze(dto);
    }

    @Operation(summary = "后台强制会员下线")
    @PostMapping("/apis/v1/auth/s/members/{memberId}/force-logout")
    @RepeatSubmit
    @SaCheckPermission("member:force-logout")
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @OperationLog(module = "会员管理", feature = "会员安全", operationType = "FORCE_LOGOUT", operationName = "强制会员下线", operationDesc = "后台强制指定会员下线并清理 token", includeResponseBody = false)
    public void forceLogoutMember(@PathVariable String memberId, @RequestBody @Valid AdminMemberForceLogoutDTO dto) {
        dto.setMemberId(RequestIdUtils.parseId(memberId, "memberId"));
        adminMemberManageService.forceLogout(dto);
    }

    /**
     * 公开接口可能会携带失效或冻结的会员 token，这里需要按匿名请求降级处理。
     */
    private Long resolveCurrentMemberIdSafely() {
        try {
            return StpMemberUtil.isLogin() ? StpMemberUtil.getLoginIdAsLong() : null;
        } catch (Exception e) {
            log.debug("会员公开接口忽略无效登录态: {}", e.getMessage(), e);
            return null;
        }
    }
}