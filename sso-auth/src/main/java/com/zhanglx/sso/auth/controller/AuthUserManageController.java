package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.annotation.RepeatSubmit;
import com.zhanglx.sso.auth.domain.dto.StatusUpdateDTO;
import com.zhanglx.sso.auth.domain.dto.UserBaseDTO;
import com.zhanglx.sso.auth.domain.dto.UserDTO;
import com.zhanglx.sso.auth.domain.dto.UserPageQueryDTO;
import com.zhanglx.sso.auth.enums.UserStatusEnum;
import com.zhanglx.sso.auth.service.UserService;
import com.zhanglx.sso.auth.service.support.AuthOperationGuard;
import com.zhanglx.sso.auth.utils.RequestIdUtils;
import com.zhanglx.sso.core.utils.AssertUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Admin user management APIs")
@RequestMapping("/apis/v1/auth/s/users")
public class AuthUserManageController {

    private final UserService userService;
    private final AuthOperationGuard authOperationGuard;

    @Operation(summary = "Create user")
    @PostMapping
    @RepeatSubmit
    @SaCheckPermission("user:add")
    public void create(@RequestBody @Valid UserDTO dto) {
        dto.setId(null);
        userService.addUser(dto);
    }

    @Operation(summary = "Update user")
    @PutMapping("/{userId}")
    @RepeatSubmit
    @SaCheckPermission("user:edit")
    public void update(@PathVariable String userId, @RequestBody @Valid UserBaseDTO dto) {
        dto.setId(RequestIdUtils.parseId(userId, "userId"));
        userService.updateUserInfo(dto);
    }

    @Operation(summary = "Delete user")
    @DeleteMapping("/{userId}")
    @RepeatSubmit
    @SaCheckPermission("user:remove")
    public void delete(@PathVariable String userId) {
        Long parsedUserId = RequestIdUtils.parseId(userId, "userId");
        authOperationGuard.checkDeleteUserNotSelf(parsedUserId);
        userService.removeUserById(parsedUserId);
    }

    @Operation(summary = "Batch delete users")
    @DeleteMapping
    @RepeatSubmit
    @SaCheckPermission("user:batch-remove")
    public void batchDelete(@RequestBody List<String> userIds) {
        AssertUtils.notEmpty(userIds, "userIds cannot be empty");
        List<Long> parsedUserIds = RequestIdUtils.parseIds(userIds, "userId");
        authOperationGuard.checkDeleteUsersNotContainsSelf(parsedUserIds);
        userService.batchRemoveUsers(parsedUserIds);
    }

    @Operation(summary = "Get user detail")
    @GetMapping("/{userId}")
    @SaCheckPermission("user:view")
    public UserDTO getById(@PathVariable String userId) {
        return userService.getUserDetail(RequestIdUtils.parseId(userId, "userId"));
    }

    @Operation(summary = "Page query users")
    @PostMapping("/page")
    @SaCheckPermission("user:list")
    public Page<UserDTO> pageQuery(@RequestBody UserPageQueryDTO queryDTO) {
        return userService.pageQuery(queryDTO);
    }

    @Operation(summary = "Update user status")
    @PatchMapping("/{userId}/status")
    @RepeatSubmit
    @SaCheckPermission("user:status")
    public void updateStatus(@PathVariable String userId, @RequestBody @Valid StatusUpdateDTO dto) {
        Long parsedUserId = RequestIdUtils.parseId(userId, "userId");
        if (UserStatusEnum.DISABLED.matches(dto.getStatus())) {
            authOperationGuard.checkDisableUserNotSelf(parsedUserId);
        }
        userService.updateStatus(parsedUserId, dto.getStatus());
    }
}
