package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.UserDTO;
import com.zhanglx.sso.auth.domain.dto.UserPageQueryDTO;
import com.zhanglx.sso.auth.service.UserService;
import com.zhanglx.sso.core.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "认证与用户管理API")
@RequestMapping("/apis/v1/user")
public class UserController {

    private final UserService userService;

    @Operation(summary = "新增用户")
    @PostMapping("/add")
    public void saveUser(@RequestBody @Validated UserDTO userDTO) {
        userDTO.setId(null);
        userService.addUser(userDTO);
    }

    @Operation(summary = "更新用户基本信息")
    @PostMapping("/update/info")
    @SaCheckPermission("user:edit")
    public void updateUserInfo(@RequestBody @Validated UserDTO userDTO) {
        userService.updateUserInfo(userDTO);
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/remove/{userId}")
    @SaCheckPermission("user:remove")
    @Parameters({
            @Parameter(name = "userId", description = "用户id", required = true, example = "1", in = ParameterIn.PATH)
    })
    public void removeUser(@PathVariable String userId) {
        if (userId.equals(String.valueOf(StpUtil.getLoginIdAsLong()))) {
            throw BusinessException.badRequest("不能删除当前登录账号");
        }

        userService.removeUserById(parseUserId(userId));
    }

    @Operation(summary = "分页查询用户列表")
    @PostMapping("/list")
    @SaCheckPermission("user:list")
    public Page<UserDTO> pageList(@Valid @RequestBody UserPageQueryDTO query) {
        return userService.pageQuery(query);
    }

    private Long parseUserId(String userId) {
        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException e) {
            throw BusinessException.badRequest("无效的用户ID: " + userId, e);
        }
    }

}
