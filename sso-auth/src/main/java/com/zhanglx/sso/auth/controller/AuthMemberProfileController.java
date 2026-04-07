package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.zhanglx.sso.auth.annotation.RepeatSubmit;
import com.zhanglx.sso.auth.domain.dto.MemberBindPhoneDTO;
import com.zhanglx.sso.auth.domain.dto.MemberUpdateDTO;
import com.zhanglx.sso.auth.domain.vo.MemberInfoVO;
import com.zhanglx.sso.auth.service.MemberUserService;
import com.zhanglx.sso.core.utils.satoken.StpMemberUtil;
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
    public MemberInfoVO current() {
        return memberUserService.getCurrentMemberInfo(StpMemberUtil.getLoginIdAsLong());
    }

    @Operation(summary = "修改当前会员资料")
    @PutMapping("/current")
    @RepeatSubmit
    @SaCheckLogin(type = StpMemberUtil.TYPE)
    public MemberInfoVO update(@RequestBody @Valid MemberUpdateDTO dto) {
        return memberUserService.updateCurrentMember(StpMemberUtil.getLoginIdAsLong(), dto);
    }

    @Operation(summary = "绑定当前会员手机号")
    @PostMapping("/current/bind-phone")
    @RepeatSubmit
    @SaCheckLogin(type = StpMemberUtil.TYPE)
    public MemberInfoVO bindPhone(@RequestBody @Valid MemberBindPhoneDTO dto) {
        return memberUserService.bindPhone(StpMemberUtil.getLoginIdAsLong(), dto);
    }

    @Operation(summary = "注销当前会员")
    @DeleteMapping("/current")
    @RepeatSubmit
    @SaCheckLogin(type = StpMemberUtil.TYPE)
    public void cancel() {
        memberUserService.cancelCurrentMember(StpMemberUtil.getLoginIdAsLong());
    }
}
