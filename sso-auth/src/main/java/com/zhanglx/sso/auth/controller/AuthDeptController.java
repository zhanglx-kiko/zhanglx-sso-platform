package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.DeptDTO;
import com.zhanglx.sso.auth.domain.dto.DeptQueryDTO;
import com.zhanglx.sso.auth.domain.dto.EnableStatusUpdateDTO;
import com.zhanglx.sso.auth.service.DeptService;
import com.zhanglx.sso.auth.utils.RequestIdUtils;
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
 * 认证部门控制器。
 */
@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "部门管理", description = "后台部门管理接口")
@RequestMapping("/apis/v1/auth/s/depts")
public class AuthDeptController {
    /**
     * 部门服务。
     */
    private final DeptService deptService;

    @Operation(summary = "新增部门")
    @PostMapping
    @RepeatSubmit
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("dept:add")
    @OperationLog(module = "部门管理", feature = "部门", operationType = "CREATE", operationName = "新增部门", operationDesc = "新增组织部门", includeResponseBody = true)
    public DeptDTO create(@RequestBody @Valid DeptDTO dto) {
        dto.setId(null);
        return deptService.create(dto);
    }

    @Operation(summary = "修改部门")
    @PutMapping("/{id}")
    @RepeatSubmit
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("dept:edit")
    @OperationLog(module = "部门管理", feature = "部门", operationType = "UPDATE", operationName = "修改部门", operationDesc = "修改组织部门", includeResponseBody = true)
    public DeptDTO update(@PathVariable String id, @RequestBody @Valid DeptDTO dto) {
        return deptService.update(RequestIdUtils.parseId(id, "deptId"), dto);
    }

    @Operation(summary = "删除部门")
    @DeleteMapping("/{id}")
    @RepeatSubmit
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("dept:remove")
    @OperationLog(module = "部门管理", feature = "部门", operationType = "DELETE", operationName = "删除部门", operationDesc = "删除组织部门", includeResponseBody = false)
    public void delete(@PathVariable String id) {
        deptService.delete(RequestIdUtils.parseId(id, "deptId"));
    }

    @Operation(summary = "部门详情")
    @GetMapping("/{id}")
    @SaCheckPermission("dept:view")
    public DeptDTO getById(@PathVariable String id) {
        return deptService.getById(RequestIdUtils.parseId(id, "deptId"));
    }

    @Operation(summary = "分页查询部门")
    @PostMapping("/page")
    @RequestRateLimit(limit = 60, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("dept:list")
    public Page<DeptDTO> pageQuery(@RequestBody DeptQueryDTO queryDTO) {
        return deptService.pageQuery(queryDTO);
    }

    @Operation(summary = "查询部门树")
    @GetMapping("/tree")
    @SaCheckPermission("dept:list")
    public List<DeptDTO> treeQuery(@RequestParam(required = false) String deptName,
                                   @RequestParam(required = false) Integer status) {
        return deptService.treeQuery(deptName, status);
    }

    @Operation(summary = "更新部门状态")
    @PatchMapping("/{id}/status")
    @RepeatSubmit
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("dept:status")
    @OperationLog(module = "部门管理", feature = "部门", operationType = "STATUS", operationName = "修改部门状态", operationDesc = "启停组织部门", includeResponseBody = true)
    public DeptDTO updateStatus(@PathVariable String id, @RequestBody @Valid EnableStatusUpdateDTO dto) {
        return deptService.updateStatus(RequestIdUtils.parseId(id, "deptId"), dto.getStatus());
    }
}