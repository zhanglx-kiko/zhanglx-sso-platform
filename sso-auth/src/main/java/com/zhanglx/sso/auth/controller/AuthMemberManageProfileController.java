package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.AdminMemberQueryDTO;
import com.zhanglx.sso.auth.domain.dto.AuthLoginLogQueryDTO;
import com.zhanglx.sso.auth.domain.vo.*;
import com.zhanglx.sso.auth.service.AdminMemberManageService;
import com.zhanglx.sso.auth.utils.RequestIdUtils;
import com.zhanglx.sso.core.domain.page.PageQuery;
import com.zhanglx.sso.web.annotation.RateLimitDimension;
import com.zhanglx.sso.web.annotation.RequestRateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会员后台资料与查询控制器。
 */
@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "会员后台查询接口")
public class AuthMemberManageProfileController {

    /**
     * 会员后台管理服务。
     */
    private final AdminMemberManageService adminMemberManageService;

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
