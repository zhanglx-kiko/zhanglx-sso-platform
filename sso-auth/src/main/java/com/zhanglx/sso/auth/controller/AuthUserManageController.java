package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.UserBaseDTO;
import com.zhanglx.sso.auth.domain.dto.UserDTO;
import com.zhanglx.sso.auth.domain.dto.UserPageQueryDTO;
import com.zhanglx.sso.auth.domain.dto.UserStatusUpdateDTO;
import com.zhanglx.sso.auth.enums.UserStatusEnum;
import com.zhanglx.sso.auth.exception.AuthManageErrorCode;
import com.zhanglx.sso.auth.service.UserService;
import com.zhanglx.sso.auth.service.support.AuthOperationGuard;
import com.zhanglx.sso.auth.utils.RequestIdUtils;
import com.zhanglx.sso.core.utils.AssertUtils;
import com.zhanglx.sso.log.annotation.OperationLog;
import com.zhanglx.sso.web.annotation.RateLimitDimension;
import com.zhanglx.sso.web.annotation.RepeatSubmit;
import com.zhanglx.sso.web.annotation.RequestRateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证用户管理控制器。
 */
@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "后台用户管理接口")
@RequestMapping("/apis/v1/auth/s/users")
public class AuthUserManageController {
    /**
     * 用户服务。
     */
    private final UserService userService;
    /**
     * 操作保护组件。
     */
    private final AuthOperationGuard authOperationGuard;

    @Operation(summary = "新增用户")
    @PostMapping
    @RepeatSubmit
    @SaCheckPermission("user:add")
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @OperationLog(module = "用户管理", feature = "用户", operationType = "CREATE", operationName = "新增用户", operationDesc = "新增后台系统用户", includeResponseBody = false)
    public void create(@RequestBody @Valid UserDTO dto) {
        dto.setId(null);
        userService.addUser(dto);
    }

    @Operation(summary = "修改用户")
    @PutMapping("/{userId}")
    @RepeatSubmit
    @SaCheckPermission("user:edit")
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @OperationLog(module = "用户管理", feature = "用户", operationType = "UPDATE", operationName = "修改用户", operationDesc = "修改后台系统用户", includeResponseBody = false)
    public void update(@PathVariable String userId, @RequestBody @Valid UserBaseDTO dto) {
        dto.setId(RequestIdUtils.parseId(userId, "userId"));
        userService.updateUserInfo(dto);
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{userId}")
    @RepeatSubmit
    @SaCheckPermission("user:remove")
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @OperationLog(module = "用户管理", feature = "用户", operationType = "DELETE", operationName = "删除用户", operationDesc = "删除单个后台用户", includeResponseBody = false)
    public void delete(@PathVariable String userId) {
        Long parsedUserId = RequestIdUtils.parseId(userId, "userId");
        authOperationGuard.checkDeleteUserNotSelf(parsedUserId);
        userService.removeUserById(parsedUserId);
    }

    @Operation(summary = "批量删除用户")
    @DeleteMapping
    @RepeatSubmit
    @SaCheckPermission("user:batch-remove")
    @RequestRateLimit(limit = 5, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @OperationLog(module = "用户管理", feature = "用户", operationType = "DELETE", operationName = "批量删除用户", operationDesc = "批量删除后台用户", includeResponseBody = false)
    public void batchDelete(@RequestBody List<String> userIds) {
        AssertUtils.notEmpty(userIds, AuthManageErrorCode.USER_IDS_EMPTY);
        List<Long> parsedUserIds = RequestIdUtils.parseIds(userIds, "userId");
        authOperationGuard.checkDeleteUsersNotContainsSelf(parsedUserIds);
        userService.batchRemoveUsers(parsedUserIds);
    }

    @Operation(summary = "用户详情")
    @GetMapping("/{userId}")
    @SaCheckPermission("user:view")
    public UserDTO getById(@PathVariable String userId) {
        return userService.getUserDetail(RequestIdUtils.parseId(userId, "userId"));
    }

    @Operation(summary = "分页查询用户")
    @PostMapping("/page")
    @SaCheckPermission("user:list")
    @RequestRateLimit(limit = 60, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    public Page<UserDTO> pageQuery(@RequestBody UserPageQueryDTO queryDTO) {
        return userService.pageQuery(queryDTO);
    }

    @Operation(summary = "更新用户状态")
    @PatchMapping("/{userId}/status")
    @RepeatSubmit
    @SaCheckPermission("user:status")
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @OperationLog(module = "用户管理", feature = "用户", operationType = "STATUS", operationName = "修改用户状态", operationDesc = "启停后台系统用户", includeResponseBody = false)
    public void updateStatus(@PathVariable String userId, @RequestBody @Valid UserStatusUpdateDTO dto) {
        Long parsedUserId = RequestIdUtils.parseId(userId, "userId");
        if (UserStatusEnum.DISABLED.matches(dto.getStatus())) {
            authOperationGuard.checkDisableUserNotSelf(parsedUserId);
        }
        userService.updateStatus(parsedUserId, dto.getStatus());
    }
}
