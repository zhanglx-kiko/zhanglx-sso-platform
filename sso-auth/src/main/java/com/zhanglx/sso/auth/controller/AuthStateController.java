package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.zhanglx.sso.auth.domain.vo.AuthSessionStatusVO;
import com.zhanglx.sso.core.utils.satoken.StpMemberUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通用认证状态控制器。
 */
@RestController
@Tag(name = "Auth State API")
@RequestMapping("/apis/v1/auth")
public class AuthStateController {

    @Operation(summary = "Probe current session status")
    @GetMapping("/isLogin")
    public AuthSessionStatusVO isLogin() {
        boolean systemLoggedIn = StpUtil.isLogin();
        boolean memberLoggedIn = StpMemberUtil.isLogin();

        return AuthSessionStatusVO.builder()
                .loggedIn(systemLoggedIn || memberLoggedIn)
                .systemLoggedIn(systemLoggedIn)
                .memberLoggedIn(memberLoggedIn)
                .build();
    }
}
