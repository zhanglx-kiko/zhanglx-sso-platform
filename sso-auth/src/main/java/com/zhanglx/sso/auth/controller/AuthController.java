package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.LoginDTO;
import com.zhanglx.sso.auth.domain.dto.UserDTO;
import com.zhanglx.sso.auth.domain.dto.UserPasswordDTO;
import com.zhanglx.sso.auth.domain.dto.UserQueryDTO;
import com.zhanglx.sso.auth.domain.vo.LoginVO;
import com.zhanglx.sso.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/10 20:50
 * @ClassName: AuthController
 * @Description: 认证与用户管理控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ==================== 1. 认证模块 (公开) ====================

    /**
     * 登录接口
     * 路径: POST /auth/login
     */
    @PostMapping("/login")
    public LoginVO login(@RequestBody @Validated LoginDTO loginDTO) {
        // GlobalResponseHandler 会自动包装为 Result<LoginVO>
        return authService.login(loginDTO);
    }

    /**
     * 注销登录
     * 路径: POST /auth/logout
     */
    @PostMapping("/logout")
    @SaCheckLogin
    public void logout() {
        StpUtil.logout();
    }

    /**
     * 查询当前登录状态 (测试用)
     * 路径: GET /auth/isLogin
     */
    @GetMapping("/isLogin")
    public String isLogin() {
        return "当前会话是否登录：" + StpUtil.isLogin();
    }

    // ==================== 2. 用户管理模块 (需鉴权) ====================

    /**
     * 新增用户
     * 路径: POST /auth/user/add
     * 权限: user:add
     */
    @PostMapping("/user/add")
    @SaCheckPermission("user:add")
    public void saveUser(@RequestBody @Validated UserDTO userDTO) {
        // 新增时 ID 应该为空
        userDTO.setId(null);
        authService.saveUser(userDTO);
    }

    /**
     * 更新用户基本信息
     * 路径: POST /auth/user/update/info
     * 权限: user:edit
     */
    @PostMapping("/user/update/info")
    @SaCheckPermission("user:edit")
    public void updateUserInfo(@RequestBody @Validated UserDTO userDTO) {
        authService.updateUserInfo(userDTO);
    }

    /**
     * 修改用户密码
     * 路径: POST /auth/user/update/password
     * 权限: 登录用户即可 (或者可以限制 user:reset-pwd)
     */
    @PostMapping("/user/update/password")
    @SaCheckLogin
    public void updatePassword(@RequestBody @Validated UserPasswordDTO passwordDTO) {
        // 在 Controller 层填充当前登录人的 ID，防止越权修改别人的密码
        passwordDTO.setUserId(StpUtil.getLoginIdAsLong());
        authService.updatePassword(passwordDTO);
    }

    /**
     * 管理员重置密码
     * 路径: POST /auth/user/reset-password/{userId}
     * 权限: user:reset
     */
    @PostMapping("/user/reset-password/{userId}")
    @SaCheckPermission("user:reset") // 需要管理员权限
    public void resetPassword(@PathVariable Long userId) {
        authService.resetPassword(userId);
    }

    /**
     * 删除用户
     * 路径: POST /auth/user/remove/{userId}
     * 权限: user:remove
     */
    @DeleteMapping("/user/remove/{userId}")
    @SaCheckPermission("user:remove")
    public void removeUser(@PathVariable Long userId) {
        // 防止删除自己
        if (userId.equals(StpUtil.getLoginIdAsLong())) {
            throw new RuntimeException("不能删除当前登录账号");
        }

        authService.removeUserById(userId);
    }

    /**
     * 分页查询用户列表
     * 路径: POST /auth/user/list
     * 权限: user:list
     */
    @PostMapping("/user/list")
    @SaCheckPermission("user:list")
    public Page<UserDTO> pageList(@RequestBody UserQueryDTO query) {
        return authService.pageQuery(query);
    }

}
