package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.AdminMemberQueryDTO;
import com.zhanglx.sso.auth.domain.dto.AuthLoginLogQueryDTO;
import com.zhanglx.sso.auth.domain.dto.MemberBindPhoneDTO;
import com.zhanglx.sso.auth.domain.dto.MemberUpdateDTO;
import com.zhanglx.sso.auth.domain.vo.AdminMemberDetailVO;
import com.zhanglx.sso.auth.domain.vo.AdminMemberListVO;
import com.zhanglx.sso.auth.domain.vo.MemberInfoVO;
import com.zhanglx.sso.auth.domain.vo.MemberLoginAuditVO;
import com.zhanglx.sso.auth.domain.vo.MemberManageRecordVO;
import com.zhanglx.sso.auth.domain.vo.MemberSocialBindingVO;
import com.zhanglx.sso.auth.service.AdminMemberManageService;
import com.zhanglx.sso.auth.service.MemberUserService;
import com.zhanglx.sso.auth.utils.RequestIdUtils;
import com.zhanglx.sso.core.domain.page.PageQuery;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 认证会员资料控制器。
 */
@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "会员中心", description = "C端会员资料接口")
public class AuthMemberProfileController {
    /**
     * 会员用户服务。
     */
    private final MemberUserService memberUserService;
    /**
     * 会员后台管理服务。
     */
    private final AdminMemberManageService adminMemberManageService;

    @Operation(summary = "查询当前会员信息")
    @GetMapping("/apis/v1/auth/m/users/current")
    @SaCheckLogin(type = StpMemberUtil.TYPE)
    @RequestRateLimit(limit = 60, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    public MemberInfoVO current() {
        return memberUserService.getCurrentMemberInfo(StpMemberUtil.getLoginIdAsLong());
    }

    @Operation(summary = "修改当前会员资料")
    @PutMapping("/apis/v1/auth/m/users/current")
    @RepeatSubmit
    @SaCheckLogin(type = StpMemberUtil.TYPE)
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @OperationLog(module = "会员中心", feature = "会员资料", operationType = "UPDATE", operationName = "修改会员资料", operationDesc = "会员修改当前账号资料", includeResponseBody = true)
    public MemberInfoVO update(@RequestBody @Valid MemberUpdateDTO dto) {
        return memberUserService.updateCurrentMember(StpMemberUtil.getLoginIdAsLong(), dto);
    }

    @Operation(summary = "绑定当前会员手机号")
    @PostMapping("/apis/v1/auth/m/users/current/bind-phone")
    @RepeatSubmit
    @SaCheckLogin(type = StpMemberUtil.TYPE)
    @RequestRateLimit(limit = 3, windowSeconds = 300, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI}, customKey = "#dto.phoneNumber")
    @OperationLog(module = "会员中心", feature = "手机号绑定", operationType = "BIND", operationName = "绑定手机号", operationDesc = "会员绑定当前手机号", includeRequestBody = false, includeResponseBody = true)
    public MemberInfoVO bindPhone(@RequestBody @Valid MemberBindPhoneDTO dto) {
        return memberUserService.bindPhone(StpMemberUtil.getLoginIdAsLong(), dto);
    }

    @Operation(summary = "注销当前会员")
    @DeleteMapping("/apis/v1/auth/m/users/current")
    @RepeatSubmit
    @SaCheckLogin(type = StpMemberUtil.TYPE)
    @RequestRateLimit(limit = 2, windowSeconds = 300, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @OperationLog(module = "会员中心", feature = "会员注销", operationType = "DELETE", operationName = "注销会员", operationDesc = "会员注销当前账号", includeResponseBody = false)
    public void cancel() {
        memberUserService.cancelCurrentMember(StpMemberUtil.getLoginIdAsLong());
    }

    @Operation(summary = "后台分页查询会员列表")
    @PostMapping("/apis/v1/auth/s/members/page")
    @SaCheckPermission("member:list")
    @RequestRateLimit(limit = 60, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    public Page<AdminMemberListVO> pageQuery(@RequestBody AdminMemberQueryDTO queryDTO) {
        return adminMemberManageService.pageQuery(queryDTO);
    }

    @Operation(summary = "后台查看会员详情")
    @GetMapping("/apis/v1/auth/s/members/{memberId}")
    @SaCheckPermission("member:view")
    @RequestRateLimit(limit = 60, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    public AdminMemberDetailVO getDetail(@PathVariable String memberId) {
        return adminMemberManageService.getDetail(RequestIdUtils.parseId(memberId, "memberId"));
    }

    @Operation(summary = "后台查询会员社交绑定信息")
    @GetMapping("/apis/v1/auth/s/members/{memberId}/social-bindings")
    @SaCheckPermission("member:social:list")
    @RequestRateLimit(limit = 60, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    public List<MemberSocialBindingVO> listSocialBindings(@PathVariable String memberId) {
        return adminMemberManageService.listSocialBindings(RequestIdUtils.parseId(memberId, "memberId"));
    }

    @Operation(summary = "后台查询会员登录审计")
    @PostMapping("/apis/v1/auth/s/members/{memberId}/login-audits/page")
    @SaCheckPermission("member:login-log:list")
    @RequestRateLimit(limit = 60, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    public Page<MemberLoginAuditVO> pageLoginAudits(@PathVariable String memberId, @RequestBody AuthLoginLogQueryDTO queryDTO) {
        return adminMemberManageService.pageLoginAudits(RequestIdUtils.parseId(memberId, "memberId"), queryDTO);
    }

    @Operation(summary = "后台查询会员管理记录")
    @PostMapping("/apis/v1/auth/s/members/{memberId}/manage-records/page")
    @SaCheckPermission("member:manage-record:list")
    @RequestRateLimit(limit = 60, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    public Page<MemberManageRecordVO> pageManageRecords(@PathVariable String memberId, @RequestBody(required = false) PageQuery pageQuery) {
        return adminMemberManageService.pageManageRecords(RequestIdUtils.parseId(memberId, "memberId"), pageQuery);
    }
}