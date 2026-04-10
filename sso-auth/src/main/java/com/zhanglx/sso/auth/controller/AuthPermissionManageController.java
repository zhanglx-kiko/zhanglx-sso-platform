package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhanglx.sso.auth.domain.dto.EnableStatusUpdateDTO;
import com.zhanglx.sso.auth.domain.dto.PermissionDTO;
import com.zhanglx.sso.auth.domain.dto.PermissionQueryDTO;
import com.zhanglx.sso.auth.domain.vo.PermissionVO;
import com.zhanglx.sso.auth.exception.AuthManageErrorCode;
import com.zhanglx.sso.auth.service.PermissionService;
import com.zhanglx.sso.auth.utils.RequestIdUtils;
import com.zhanglx.sso.core.utils.AssertUtils;
import com.zhanglx.sso.log.annotation.OperationLog;
import com.zhanglx.sso.web.annotation.RateLimitDimension;
import com.zhanglx.sso.web.annotation.RepeatSubmit;
import com.zhanglx.sso.web.annotation.RequestRateLimit;
import com.zhanglx.sso.xss.annotation.XssPolicy;
import com.zhanglx.sso.xss.support.XssPolicyMode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证权限管理控制器。
 */
@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "权限管理", description = "后台权限管理接口")
@RequestMapping("/apis/v1/auth/s/permissions")
public class AuthPermissionManageController {
    /**
     * 权限服务。
     */
    private final PermissionService permissionService;

    @Operation(summary = "新增权限")
    @PostMapping
    @RepeatSubmit
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("permission:add")
    @OperationLog(module = "权限管理", feature = "权限", operationType = "CREATE", operationName = "新增权限", operationDesc = "新增权限节点", includeResponseBody = true)
    public PermissionDTO create(@RequestBody @Valid PermissionDTO dto) {
        dto.setId(null);
        return permissionService.addPermission(dto);
    }

    @Operation(summary = "修改权限")
    @PutMapping("/{id}")
    @RepeatSubmit
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("permission:edit")
    @OperationLog(module = "权限管理", feature = "权限", operationType = "UPDATE", operationName = "修改权限", operationDesc = "修改权限节点", includeResponseBody = true)
    public PermissionDTO update(@PathVariable String id, @RequestBody @Valid PermissionDTO dto) {
        return permissionService.updatePermission(RequestIdUtils.parseId(id, "permissionId"), dto);
    }

    @Operation(summary = "删除权限")
    @DeleteMapping("/{id}")
    @RepeatSubmit
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("permission:remove")
    @OperationLog(module = "权限管理", feature = "权限", operationType = "DELETE", operationName = "删除权限", operationDesc = "删除单个权限节点", includeResponseBody = false)
    public PermissionDTO delete(@PathVariable String id) {
        return permissionService.delPermission(RequestIdUtils.parseId(id, "permissionId"));
    }

    @Operation(summary = "批量删除权限")
    @DeleteMapping
    @RepeatSubmit
    @RequestRateLimit(limit = 5, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("permission:remove")
    @OperationLog(module = "权限管理", feature = "权限", operationType = "DELETE", operationName = "批量删除权限", operationDesc = "批量删除权限节点", includeResponseBody = false)
    public List<PermissionDTO> batchDelete(@RequestBody List<String> ids) {
        AssertUtils.notEmpty(ids, AuthManageErrorCode.PERMISSION_IDS_EMPTY);
        return permissionService.batchDelPermission(RequestIdUtils.parseIds(ids, "permissionId"));
    }

    @Operation(summary = "权限详情")
    @GetMapping("/{id}")
    @SaCheckPermission("permission:view")
    public PermissionDTO getById(@PathVariable String id) {
        return permissionService.getPermission(RequestIdUtils.parseId(id, "permissionId"));
    }

    @Operation(summary = "查询权限树")
    @GetMapping("/tree")
    @SaCheckPermission("permission:list")
    public List<PermissionDTO> tree(
            @XssPolicy(XssPolicyMode.SEARCH)
            @RequestParam(required = false, defaultValue = "") String searchKey
    ) {
        return permissionService.selPermission(searchKey);
    }

    @Operation(summary = "按标识查询权限")
    @PostMapping("/by-identification")
    @RequestRateLimit(limit = 30, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("permission:list")
    public List<PermissionVO> byIdentification(@RequestBody PermissionQueryDTO queryDTO) {
        return permissionService.selPermissionByIdentification(
                queryDTO.getUsername(),
                queryDTO.getIdentifications(),
                queryDTO.getPermissionTypes()
        );
    }

    @Operation(summary = "更新权限状态")
    @PatchMapping("/{id}/status")
    @RepeatSubmit
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("permission:status")
    @OperationLog(module = "权限管理", feature = "权限", operationType = "STATUS", operationName = "修改权限状态", operationDesc = "启停权限节点", includeResponseBody = true)
    public PermissionDTO updateStatus(@PathVariable String id, @RequestBody @Valid EnableStatusUpdateDTO dto) {
        return permissionService.updateStatus(RequestIdUtils.parseId(id, "permissionId"), dto.getStatus());
    }
}
