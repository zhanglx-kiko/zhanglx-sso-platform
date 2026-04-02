package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.zhanglx.sso.auth.domain.dto.ForgotPasswordDTO;
import com.zhanglx.sso.auth.domain.dto.LoginDTO;
import com.zhanglx.sso.auth.domain.dto.UserPasswordDTO;
import com.zhanglx.sso.auth.domain.vo.LoginVO;
import com.zhanglx.sso.auth.service.AuthService;
import com.zhanglx.sso.auth.service.WechatAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/10 20:50
 * @ClassName: AuthController
 * @Description: 认证与用户管理控制器
 * <p>
 * 主要职责：
 * 1. 用户登录、注销等认证操作（公开接口）
 * 2. 用户密码管理（需鉴权）
 * 3. 微信授权登录
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "认证与用户管理API")
@RequestMapping("/apis/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final WechatAuthService wechatAuthService;
    // ==================== 1. 认证模块 (公开) ====================

    /**
     * 登录接口
     * 路径：POST /apis/v1/auth/login
     * <p>
     * 业务逻辑：
     * 1. 校验账号密码是否为空
     * 2. 查询用户信息
     * 3. 比对密码（使用 Argon2 加密算法）
     * 4. 检查账户状态（是否禁用）
     * 5. 处理互顶/并发控制
     * 6. 执行登录并返回 Token
     *
     * @param loginDTO 登录参数，包含用户名、密码、设备标识
     * @return LoginVO 登录结果，包含用户信息和 Token
     */
    @Operation(summary = "登录接口")
    @PostMapping("/login")
    public LoginVO login(@RequestBody @Validated LoginDTO loginDTO) {
        // GlobalResponseHandler 会自动包装为 Result<LoginVO>
        return authService.login(loginDTO);
    }

    /**
     * 微信授权登录
     * 路径：POST /apis/v1/auth/wechat/login
     * <p>
     * 业务逻辑：
     * 1. 接收小程序端 wx.login() 返回的 code
     * 2. 调用微信接口换取 openid
     * 3. 根据 openid 查询或创建用户
     * 4. 执行 Sa-Token 登录
     *
     * @param code 微信授权码（由小程序端 wx.login() 获取）
     * @return LoginVO 登录结果，包含用户信息和 Token
     */
    @PostMapping("/wechat/login")
    public LoginVO wechatLogin(@RequestParam String code) {
        return wechatAuthService.loginByWechatCode(code);
    }

//    @PostMapping("/login/wechat")
//    public LoginVO loginByWechat(@RequestBody WechatLoginDTO dto) {
    // 1. 拿着 dto.code 去请求微信接口，获取微信 openid
    // 2. 去数据库查：SELECT user_id FROM sys_user_bind WHERE openid = ?
    // 3. 如果查到了关联的用户 ID (比如查到 userId = 1001)
    // 4. 直接执行：StpUtil.login(1001, "Wechat")
    // 5. 返回 Token 给前端
//    }

    /**
     * 注销登录
     * 路径：POST /apis/v1/auth/logout
     * <p>
     * 业务逻辑：
     * 1. 校验用户是否已登录（@SaCheckLogin）
     * 2. 清除当前用户的登录会话
     * 3. 使 Token 失效
     */
    @Operation(summary = "注销登录")
    @PostMapping("/logout")
    @SaCheckLogin
    public void logout() {
        StpUtil.logout();
    }

    /**
     * 查询当前登录状态（测试用）
     * 路径：GET /apis/v1/auth/isLogin
     * <p>
     * 主要用于调试和前端轮询检测登录状态
     *
     * @return String 返回当前会话是否登录的文本描述
     */
    @Operation(summary = "查询当前登录状态 (测试用)")
    @GetMapping("/isLogin")
    public String isLogin() {
        return "当前会话是否登录：" + StpUtil.isLogin();
    }

    // ==================== 2. 用户管理模块 (需鉴权) ====================

    /**
     * 修改用户密码
     * 路径：POST /apis/v1/auth/user/update/password
     * 权限：登录用户即可
     * <p>
     * 业务逻辑：
     * 1. 校验用户是否登录
     * 2. 获取当前登录用户 ID（防止越权修改）
     * 3. 验证旧密码
     * 4. 加密并更新新密码
     * 5. 强制踢出所有在线设备（安全考虑）
     *
     * @param passwordDTO 密码修改参数，包含旧密码和新密码
     */
    @Operation(summary = "修改用户密码")
    @PostMapping("/user/update/password")
    @SaCheckLogin
    public void updatePassword(@RequestBody @Validated UserPasswordDTO passwordDTO) {
        // 在 Controller 层填充当前登录人的 ID，防止越权修改别人的密码
        passwordDTO.setUserId(StpUtil.getLoginIdAsLong());
        authService.updatePassword(passwordDTO);
    }

    /**
     * 管理员重置密码
     * 路径：POST /apis/v1/auth/user/reset-password/{userId}
     * 权限：user:reset
     * <p>
     * 业务逻辑：
     * 1. 校验管理员权限
     * 2. 将用户密码重置为默认密码（从配置读取）
     * 3. 强制踢出该用户所有在线会话（安全必须）
     * 4. 记录操作日志
     *
     * @param userId 用户 ID（路径参数）
     */
    @Operation(summary = "管理员重置密码")
    @PostMapping("/user/reset-password/{userId}")
    @SaCheckPermission("user:reset") // 需要管理员权限
    @Parameters({
            @Parameter(name = "userId", description = "用户id", required = true, in = ParameterIn.PATH)
    })
    public void resetPassword(@PathVariable String userId) {
        authService.resetPassword(Long.parseLong(userId));
    }

    /**
     * 忘记密码 - 通过验证码重置密码
     * 路径：POST /apis/v1/auth/forgot-password
     * 权限：公开（无需登录）
     * <p>
     * 业务逻辑：
     * 1. 校验用户名和新密码是否为空
     * 2. 查询用户信息
     * 3. 验证验证码是否正确
     * 4. 加密并更新新密码
     * 5. 强制踢出该用户所有在线会话（安全必须）
     *
     * @param forgotPasswordDTO 忘记密码参数，包含用户名、新密码、验证码
     */
    @Operation(summary = "忘记密码 - 通过验证码重置密码")
    @PostMapping("/forgot-password")
    public void forgotPassword(@RequestBody @Validated ForgotPasswordDTO forgotPasswordDTO) {
        authService.forgotPassword(forgotPasswordDTO);
    }

}
