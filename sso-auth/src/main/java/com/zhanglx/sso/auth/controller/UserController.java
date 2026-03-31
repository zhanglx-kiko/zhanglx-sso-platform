package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.UserDTO;
import com.zhanglx.sso.auth.domain.dto.UserQueryDTO;
import com.zhanglx.sso.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/10 20:50
 * @ClassName: AuthController
 * @Description: 用户管理控制器
 * <p>
 * 主要职责：
 * 1. 用户基本信息管理（增删改查）
 * 2. 用户状态管理
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "认证与用户管理API")
@RequestMapping("/apis/v1/user")
public class UserController {

    private final UserService userService;

    /**
     * 新增用户
     * 路径：POST /apis/v1/auth/user/add
     * 权限：user:add
     * <p>
     * 业务逻辑：
     * 1. 校验用户名唯一性
     * 2. 加密密码（使用 Argon2 算法）
     * 3. 设置默认状态和并发策略
     * 4. 保存用户信息
     *
     * @param userDTO 用户信息对象（ID 应为空）
     */
    @Operation(summary = "新增用户")
    @PostMapping("/add")
//    @SaCheckPermission("user:add")
    public void saveUser(@RequestBody @Validated UserDTO userDTO) {
        // 新增时 ID 应该为空
        userDTO.setId(null);
        userService.addUser(userDTO);
    }

    /**
     * 更新用户基本信息
     * 路径: POST /auth/user/update/info
     * 权限: user:edit
     */
    @Operation(summary = "更新用户基本信息")
    @PostMapping("/update/info")
    @SaCheckPermission("user:edit")
    public void updateUserInfo(@RequestBody @Validated UserDTO userDTO) {
        userService.updateUserInfo(userDTO);
    }

    /**
     * 删除用户
     * 路径：POST /auth/user/remove/{userId}
     * 权限：user:remove
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/remove/{userId}")
    @SaCheckPermission("user:remove")
    @Parameters({
//            @Parameter(name = "file", description = "单文件上传", required = true, schema = @Schema(type = "file", format = "binary"), in = ParameterIn.DEFAULT),
            @Parameter(name = "userId", description = "用户id", required = true, example = "1", in = ParameterIn.PATH)
    })
    public void removeUser(@PathVariable String userId) {
        // 防止删除自己
        if (userId.equals(String.valueOf(StpUtil.getLoginIdAsLong()))) {
            throw new RuntimeException("不能删除当前登录账号");
        }

        userService.removeUserById(Long.parseLong(userId));
    }

    /**
     * 分页查询用户列表
     * 路径: POST /auth/user/list
     * 权限: user:list
     */
    @Operation(summary = "分页查询用户列表")
    @PostMapping("/list")
    @SaCheckPermission("user:list")
    public Page<UserDTO> pageList(@Valid @RequestBody UserQueryDTO query) {
        return userService.pageQuery(query);
    }

}
