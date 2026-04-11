package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhanglx.sso.auth.domain.dto.AdminMemberForceLogoutDTO;
import com.zhanglx.sso.auth.domain.dto.AdminMemberStatusUpdateDTO;
import com.zhanglx.sso.auth.service.AdminMemberManageService;
import com.zhanglx.sso.auth.utils.RequestIdUtils;
import com.zhanglx.sso.log.annotation.OperationLog;
import com.zhanglx.sso.web.annotation.RateLimitDimension;
import com.zhanglx.sso.web.annotation.RepeatSubmit;
import com.zhanglx.sso.web.annotation.RequestRateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 会员后台状态与安全管理控制器。
 */
@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "会员后台管理接口")
public class AuthMemberManageController {

    /**
     * 会员后台管理服务。
     */
    private final AdminMemberManageService adminMemberManageService;

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
}
