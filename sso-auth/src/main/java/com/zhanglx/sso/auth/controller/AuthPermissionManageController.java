package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhanglx.sso.auth.annotation.RepeatSubmit;
import com.zhanglx.sso.auth.domain.dto.PermissionDTO;
import com.zhanglx.sso.auth.domain.dto.PermissionQueryDTO;
import com.zhanglx.sso.auth.domain.dto.StatusUpdateDTO;
import com.zhanglx.sso.auth.domain.vo.PermissionVO;
import com.zhanglx.sso.auth.service.PermissionService;
import com.zhanglx.sso.auth.utils.RequestIdUtils;
import com.zhanglx.sso.core.utils.AssertUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "权限管理", description = "B 端权限管理接口")
@RequestMapping("/apis/v1/auth/s/permissions")
public class AuthPermissionManageController {

    private final PermissionService permissionService;

    @Operation(summary = "新增权限")
    @PostMapping
    @RepeatSubmit
    @SaCheckPermission("permission:add")
    public PermissionDTO create(@RequestBody @Valid PermissionDTO dto) {
        dto.setId(null);
        return permissionService.addPermission(dto);
    }

    @Operation(summary = "修改权限")
    @PutMapping("/{id}")
    @RepeatSubmit
    @SaCheckPermission("permission:edit")
    public PermissionDTO update(@PathVariable String id, @RequestBody @Valid PermissionDTO dto) {
        return permissionService.updatePermission(RequestIdUtils.parseId(id, "权限ID"), dto);
    }

    @Operation(summary = "删除权限")
    @DeleteMapping("/{id}")
    @RepeatSubmit
    @SaCheckPermission("permission:remove")
    public PermissionDTO delete(@PathVariable String id) {
        return permissionService.delPermission(RequestIdUtils.parseId(id, "权限ID"));
    }

    @Operation(summary = "批量删除权限")
    @DeleteMapping
    @RepeatSubmit
    @SaCheckPermission("permission:remove")
    public List<PermissionDTO> batchDelete(@RequestBody List<String> ids) {
        AssertUtils.notEmpty(ids, "权限 ID 列表不能为空");
        return permissionService.batchDelPermission(RequestIdUtils.parseIds(ids, "权限ID"));
    }

    @Operation(summary = "权限详情")
    @GetMapping("/{id}")
    @SaCheckPermission("permission:view")
    public PermissionDTO getById(@PathVariable String id) {
        return permissionService.getPermission(RequestIdUtils.parseId(id, "权限ID"));
    }

    @Operation(summary = "查询权限树")
    @GetMapping("/tree")
    @SaCheckPermission("permission:list")
    public List<PermissionDTO> tree(@RequestParam(required = false, defaultValue = "") String searchKey) {
        return permissionService.selPermission(searchKey);
    }

    @Operation(summary = "按标识查询权限")
    @PostMapping("/by-identification")
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
    @SaCheckPermission("permission:status")
    public PermissionDTO updateStatus(@PathVariable String id, @RequestBody @Valid StatusUpdateDTO dto) {
        return permissionService.updateStatus(RequestIdUtils.parseId(id, "权限ID"), dto.getStatus());
    }
}
