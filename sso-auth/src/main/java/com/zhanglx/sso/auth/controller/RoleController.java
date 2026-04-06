package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.RoleDTO;
import com.zhanglx.sso.auth.domain.dto.RolePermissionRelationshipMappingDTO;
import com.zhanglx.sso.auth.domain.vo.RoleInfoVO;
import com.zhanglx.sso.auth.service.RoleService;
import com.zhanglx.sso.core.domain.page.PageQuery;
import com.zhanglx.sso.core.utils.AssertUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/30 15:37
 * @ClassName: RoleController
 * @Description: 角色管理控制器
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "角色信息的增删改查及权限分配")
@RequestMapping("/apis/v1/roles")
public class RoleController {

    private final RoleService roleService;

    // ==================== 1. 角色基础管理 ====================

    /**
     * 新增角色
     * 路径：POST /apis/v1/roles
     * 权限：role:add
     */
    @Operation(summary = "新增角色")
    @PostMapping
    @SaCheckPermission("role:add")
    public RoleDTO addRole(@RequestBody @Validated RoleDTO roleDTO) {
        return roleService.addRole(roleDTO);
    }

    /**
     * 修改角色信息
     * 路径：PUT /apis/v1/roles/{id}
     * 权限：role:edit
     */
    @Operation(summary = "修改角色信息")
    @PutMapping("/{id}")
    @SaCheckPermission("role:edit")
    @Parameter(name = "id", description = "角色 ID", required = true, in = ParameterIn.PATH)
    public RoleDTO updateRole(@PathVariable Long id, @RequestBody @Validated RoleDTO roleDTO) {
        return roleService.updateRole(id, roleDTO);
    }

    /**
     * 删除角色（单个）
     * 路径：DELETE /apis/v1/roles/{id}
     * 权限：role:remove
     */
    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @SaCheckPermission("role:remove")
    @Parameter(name = "id", description = "角色 ID", required = true, in = ParameterIn.PATH)
    public RoleDTO delRole(@PathVariable Long id) {
        return roleService.delRole(id);
    }

    /**
     * 批量删除角色
     * 路径：DELETE /apis/v1/roles/batch
     * 权限：role:remove
     */
    @Operation(summary = "批量删除角色")
    @DeleteMapping("/batch")
    @SaCheckPermission("role:remove")
    public void batchDelRole(@RequestBody List<Long> idList) {
        AssertUtils.notEmpty(idList, "角色 ID 列表不能为空");
        roleService.batchDelRole(idList);
    }

    /**
     * 分页查询角色列表
     * 路径：POST /apis/v1/roles/page
     * 权限：role:list
     */
    @Operation(summary = "分页查询角色列表")
    @PostMapping("/page")
    @SaCheckPermission("role:list")
    public Page<RoleDTO> pageList(@RequestBody PageQuery queryParam) {
        return roleService.selRole(queryParam);
    }

    /**
     * 获取角色详情
     * 路径：GET /apis/v1/roles/{roleId}
     * 权限：role:view
     */
    @Operation(summary = "获取角色详情")
    @GetMapping("/{roleId}")
    @SaCheckPermission("role:view")
    @Parameter(name = "roleId", description = "角色 ID", required = true, in = ParameterIn.PATH)
    public RoleDTO getRoleDetail(@PathVariable Long roleId) {
        return roleService.loadRole(roleId);
    }

    // ==================== 2. 角色用户管理 ====================

    /**
     * 获取角色详情（用于分配用户）
     * 路径：GET /apis/v1/roles/{roleId}/users
     * 权限：role:view
     */
    @Operation(summary = "获取角色详情（包含关联用户 ID 列表）")
    @GetMapping("/{roleId}/users")
    @SaCheckPermission("role:view")
    @Parameter(name = "roleId", description = "角色 ID", required = true, in = ParameterIn.PATH)
    public RoleInfoVO getRoleWithUsers(@PathVariable Long roleId) {
        return roleService.selectRoleDetail(roleId);
    }

    /**
     * 为角色绑定用户
     * 路径：POST /apis/v1/roles/{roleId}/users
     * 权限：role:bind-user
     */
    @Operation(summary = "为角色绑定用户")
    @PostMapping("/{roleId}/users")
    @SaCheckPermission("role:bind-user")
    @Parameter(name = "roleId", description = "角色 ID", required = true, in = ParameterIn.PATH)
    public RoleInfoVO bindUsers(@PathVariable Long roleId, @RequestBody List<Long> userIds) {
        return roleService.bindUsers(roleId, userIds);
    }

    // ==================== 3. 角色权限管理 ====================

    /**
     * 为角色分配权限
     * 路径：POST /apis/v1/roles/{roleId}/permissions
     * 权限：role:assign-permission
     */
    @Operation(summary = "为角色分配权限")
    @PostMapping("/{roleId}/permissions")
    @SaCheckPermission("role:assign-permission")
    @Parameter(name = "roleId", description = "角色 ID", required = true, in = ParameterIn.PATH)
    public RoleDTO associatePermissions(
            @PathVariable Long roleId,
            @RequestBody List<RolePermissionRelationshipMappingDTO> permissions
    ) {
        return roleService.associatePermissions(roleId, permissions);
    }

    // ==================== 4. 用户角色查询 ====================

    /**
     * 查询当前登录用户的角色列表
     * 路径：GET /apis/v1/roles/my-roles
     * 权限：登录即可
     */
    @Operation(summary = "查询当前登录用户的角色列表")
    @GetMapping("/my-roles")
    @SaCheckLogin
    public List<RoleDTO> getMyRoles() {
        String username = StpUtil.getLoginIdAsString();
        return roleService.selectRolesForUser(username);
    }

    /**
     * 根据用户 ID 查询用户关联的角色列表
     * 路径：GET /apis/v1/roles/user/{userId}
     * 权限：role:view
     */
    @Operation(summary = "根据用户 ID 查询用户关联的角色列表")
    @GetMapping("/user/{userId}")
    @SaCheckPermission("role:view")
    @Parameter(name = "userId", description = "用户 ID", required = true, in = ParameterIn.PATH)
    public List<RoleDTO> getRolesByUserId(@PathVariable Long userId) {
        return roleService.selectRolesForUser(userId);
    }

}
