package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.zhanglx.sso.auth.domain.dto.MemberBindPhoneDTO;
import com.zhanglx.sso.auth.domain.dto.MemberUpdateDTO;
import com.zhanglx.sso.auth.domain.vo.MemberInfoVO;
import com.zhanglx.sso.auth.service.MemberUserService;
import com.zhanglx.sso.core.utils.satoken.StpMemberUtil;
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

@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "会员中心", description = "C 端会员资料接口")
@RequestMapping("/apis/v1/auth/m/users")
public class AuthMemberProfileController {

    private final MemberUserService memberUserService;

    @Operation(summary = "查询当前会员信息")
    @GetMapping("/current")
    @SaCheckLogin(type = StpMemberUtil.TYPE)
    @RequestRateLimit(limit = 60, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    public MemberInfoVO current() {
        return memberUserService.getCurrentMemberInfo(StpMemberUtil.getLoginIdAsLong());
    }

    @Operation(summary = "修改当前会员资料")
    @PutMapping("/current")
    @RepeatSubmit
    @SaCheckLogin(type = StpMemberUtil.TYPE)
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @OperationLog(module = "会员中心", feature = "会员资料", operationType = "UPDATE", operationName = "修改会员资料", operationDesc = "会员修改当前账号资料", includeResponseBody = true)
    public MemberInfoVO update(@RequestBody @Valid MemberUpdateDTO dto) {
        return memberUserService.updateCurrentMember(StpMemberUtil.getLoginIdAsLong(), dto);
    }

    @Operation(summary = "绑定当前会员手机号")
    @PostMapping("/current/bind-phone")
    @RepeatSubmit
    @SaCheckLogin(type = StpMemberUtil.TYPE)
    @RequestRateLimit(limit = 3, windowSeconds = 300, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI}, customKey = "#dto.phoneNumber")
    @OperationLog(module = "会员中心", feature = "手机号绑定", operationType = "BIND", operationName = "绑定手机号", operationDesc = "会员绑定当前手机号", includeRequestBody = false, includeResponseBody = true)
    public MemberInfoVO bindPhone(@RequestBody @Valid MemberBindPhoneDTO dto) {
        return memberUserService.bindPhone(StpMemberUtil.getLoginIdAsLong(), dto);
    }

    @Operation(summary = "注销当前会员")
    @DeleteMapping("/current")
    @RepeatSubmit
    @SaCheckLogin(type = StpMemberUtil.TYPE)
    @RequestRateLimit(limit = 2, windowSeconds = 300, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @OperationLog(module = "会员中心", feature = "会员注销", operationType = "DELETE", operationName = "注销会员", operationDesc = "会员注销当前账号", includeResponseBody = false)
    public void cancel() {
        memberUserService.cancelCurrentMember(StpMemberUtil.getLoginIdAsLong());
    }
}
