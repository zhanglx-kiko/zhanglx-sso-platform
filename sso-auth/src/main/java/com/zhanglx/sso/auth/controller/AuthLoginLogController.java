package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.AuthLoginLogQueryDTO;
import com.zhanglx.sso.auth.domain.vo.AuthLoginLogVO;
import com.zhanglx.sso.auth.service.AuthLoginLogService;
import com.zhanglx.sso.auth.utils.RequestIdUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 登录日志后台接口。
 */
@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "登录日志")
@RequestMapping("/apis/v1/auth/s/login-logs")
public class AuthLoginLogController {

    private final AuthLoginLogService authLoginLogService;

    @Operation(summary = "分页查询登录日志")
    @PostMapping("/page")
    @SaCheckPermission("login-log:list")
    public Page<AuthLoginLogVO> pageQuery(@RequestBody AuthLoginLogQueryDTO queryDTO) {
        return authLoginLogService.pageQuery(queryDTO);
    }

    @Operation(summary = "查看登录日志详情")
    @GetMapping("/{id}")
    @SaCheckPermission("login-log:view")
    public AuthLoginLogVO detail(@PathVariable String id) {
        return authLoginLogService.getDetail(RequestIdUtils.parseId(id, "loginLogId"));
    }
}
