package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.zhanglx.sso.auth.domain.dto.MemberBasicBatchQueryDTO;
import com.zhanglx.sso.auth.domain.vo.MemberBasicVO;
import com.zhanglx.sso.auth.service.MemberUserService;
import com.zhanglx.sso.core.utils.satoken.StpMemberUtil;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Hidden
@Validated
@RestController
@RequiredArgsConstructor
public class AuthMemberInternalController {

    private final MemberUserService memberUserService;

    @Operation(summary = "查询当前登录会员基础信息")
    @GetMapping("/apis/v1/auth/internal/members/current/basic")
    @SaCheckLogin(type = StpMemberUtil.TYPE)
    public MemberBasicVO currentBasic() {
        return memberUserService.getCurrentMemberBasicInfo(StpMemberUtil.getLoginIdAsLong());
    }

    @Operation(summary = "批量查询会员基础信息")
    @PostMapping("/apis/v1/auth/internal/members/basic/query")
    public List<MemberBasicVO> queryBasics(@RequestBody @Valid MemberBasicBatchQueryDTO queryDTO) {
        return memberUserService.listMemberBasicInfo(queryDTO.getMemberIds());
    }
}
