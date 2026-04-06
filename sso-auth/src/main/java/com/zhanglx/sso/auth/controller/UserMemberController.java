package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.zhanglx.sso.auth.domain.dto.MemberBindPhoneDTO;
import com.zhanglx.sso.auth.domain.dto.MemberUpdateDTO;
import com.zhanglx.sso.auth.domain.vo.MemberInfoVO;
import com.zhanglx.sso.auth.service.MemberUserService;
import com.zhanglx.sso.core.utils.satoken.StpMemberUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "C端会员中心接口")
@RequestMapping("/apis/v1/user/m")
public class UserMemberController {

    private final MemberUserService memberUserService;

    @Operation(summary = "查询当前会员信息")
    @GetMapping("/info")
    @SaCheckLogin(type = StpMemberUtil.TYPE)
    public MemberInfoVO getCurrentMemberInfo() {
        return memberUserService.getCurrentMemberInfo(StpMemberUtil.getLoginIdAsLong());
    }

    @Operation(summary = "更新当前会员资料")
    @PostMapping("/update/info")
    @SaCheckLogin(type = StpMemberUtil.TYPE)
    public MemberInfoVO updateCurrentMember(@RequestBody @Validated MemberUpdateDTO updateDTO) {
        return memberUserService.updateCurrentMember(StpMemberUtil.getLoginIdAsLong(), updateDTO);
    }

    @Operation(summary = "绑定当前会员手机号")
    @PostMapping("/bind-phone")
    @SaCheckLogin(type = StpMemberUtil.TYPE)
    public MemberInfoVO bindPhone(@RequestBody @Validated MemberBindPhoneDTO bindPhoneDTO) {
        return memberUserService.bindPhone(StpMemberUtil.getLoginIdAsLong(), bindPhoneDTO);
    }

    @Operation(summary = "注销当前会员")
    @DeleteMapping("/cancel")
    @SaCheckLogin(type = StpMemberUtil.TYPE)
    public void cancelCurrentMember() {
        memberUserService.cancelCurrentMember(StpMemberUtil.getLoginIdAsLong());
    }
}
