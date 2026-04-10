package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.EnableStatusUpdateDTO;
import com.zhanglx.sso.auth.domain.dto.RoleDTO;
import com.zhanglx.sso.auth.domain.dto.RolePermissionRelationshipMappingDTO;
import com.zhanglx.sso.auth.domain.vo.RoleInfoVO;
import com.zhanglx.sso.auth.service.RoleService;
import com.zhanglx.sso.auth.utils.RequestIdUtils;
import com.zhanglx.sso.core.domain.page.PageQuery;
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
 * 认证角色管理控制器。
 */
@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "后台角色管理接口")
@RequestMapping("/apis/v1/auth/s/roles")
public class AuthRoleManageController {
    /**
     * 角色服务。
     */
    private final RoleService roleService;

    @Operation(summary = "新增角色")
    @PostMapping
    @RepeatSubmit
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("role:add")
    @OperationLog(module = "角色管理", feature = "角色", operationType = "CREATE", operationName = "新增角色", operationDesc = "新增后台角色", includeResponseBody = true)
    public RoleDTO create(@RequestBody @Valid RoleDTO dto) {
        dto.setId(null);
        return roleService.addRole(dto);
    }

    @Operation(summary = "修改角色")
    @PutMapping("/{id}")
    @RepeatSubmit
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("role:edit")
    @OperationLog(module = "角色管理", feature = "角色", operationType = "UPDATE", operationName = "修改角色", operationDesc = "修改后台角色", includeResponseBody = true)
    public RoleDTO update(@PathVariable String id, @RequestBody @Valid RoleDTO dto) {
        return roleService.updateRole(RequestIdUtils.parseId(id, "roleId"), dto);
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @RepeatSubmit
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("role:remove")
    @OperationLog(module = "角色管理", feature = "角色", operationType = "DELETE", operationName = "删除角色", operationDesc = "删除单个后台角色", includeResponseBody = false)
    public RoleDTO delete(@PathVariable String id) {
        return roleService.delRole(RequestIdUtils.parseId(id, "roleId"));
    }

    @Operation(summary = "批量删除角色")
    @DeleteMapping
    @RepeatSubmit
    @RequestRateLimit(limit = 5, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("role:remove")
    @OperationLog(module = "角色管理", feature = "角色", operationType = "DELETE", operationName = "批量删除角色", operationDesc = "批量删除后台角色", includeResponseBody = false)
    public void batchDelete(@RequestBody List<String> ids) {
        AssertUtils.notEmpty(ids, "角色 ID 列表不能为空");
        roleService.batchDelRole(RequestIdUtils.parseIds(ids, "roleId"));
    }

    @Operation(summary = "角色详情")
    @GetMapping("/{id}")
    @SaCheckPermission("role:view")
    public RoleDTO getById(@PathVariable String id) {
        return roleService.loadRole(RequestIdUtils.parseId(id, "roleId"));
    }

    @Operation(summary = "分页查询角色")
    @PostMapping("/page")
    @RequestRateLimit(limit = 60, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("role:list")
    public Page<RoleDTO> pageQuery(@RequestBody PageQuery query) {
        return roleService.selRole(query);
    }

    @Operation(summary = "更新角色状态")
    @PatchMapping("/{id}/status")
    @RepeatSubmit
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("role:status")
    @OperationLog(module = "角色管理", feature = "角色", operationType = "STATUS", operationName = "修改角色状态", operationDesc = "启停后台角色", includeResponseBody = true)
    public RoleDTO updateStatus(@PathVariable String id, @RequestBody @Valid EnableStatusUpdateDTO dto) {
        return roleService.updateStatus(RequestIdUtils.parseId(id, "roleId"), dto.getStatus());
    }

    @Operation(summary = "查询角色绑定用户")
    @GetMapping("/{roleId}/users")
    @SaCheckPermission("role:view")
    public RoleInfoVO getRoleUsers(@PathVariable String roleId) {
        return roleService.selectRoleDetail(RequestIdUtils.parseId(roleId, "roleId"));
    }

    @Operation(summary = "绑定角色用户")
    @PutMapping("/{roleId}/users")
    @RepeatSubmit
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("role:bind-user")
    @OperationLog(module = "角色管理", feature = "角色用户绑定", operationType = "BIND", operationName = "绑定角色用户", operationDesc = "维护角色与用户的绑定关系", includeResponseBody = true)
    public RoleInfoVO bindUsers(@PathVariable String roleId, @RequestBody List<String> userIds) {
        return roleService.bindUsers(
                RequestIdUtils.parseId(roleId, "roleId"),
                RequestIdUtils.parseIds(userIds, "userId")
        );
    }

    @Operation(summary = "绑定角色权限")
    @PutMapping("/{roleId}/permissions")
    @RepeatSubmit
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("role:assign-permission")
    @OperationLog(module = "角色管理", feature = "角色权限绑定", operationType = "BIND", operationName = "绑定角色权限", operationDesc = "维护角色与权限的绑定关系", includeResponseBody = true)
    public RoleDTO bindPermissions(@PathVariable String roleId,
                                   @RequestBody List<RolePermissionRelationshipMappingDTO> permissions) {
        return roleService.associatePermissions(RequestIdUtils.parseId(roleId, "roleId"), permissions);
    }

    @Operation(summary = "查询当前登录用户角色")
    @GetMapping("/my")
    @SaCheckLogin
    public List<RoleDTO> myRoles() {
        return roleService.selectRolesForUser(StpUtil.getLoginIdAsString());
    }
}