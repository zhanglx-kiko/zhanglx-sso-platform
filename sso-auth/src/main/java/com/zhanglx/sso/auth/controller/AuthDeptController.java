package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.annotation.RepeatSubmit;
import com.zhanglx.sso.auth.domain.dto.DeptDTO;
import com.zhanglx.sso.auth.domain.dto.DeptQueryDTO;
import com.zhanglx.sso.auth.domain.dto.StatusUpdateDTO;
import com.zhanglx.sso.auth.service.DeptService;
import com.zhanglx.sso.auth.utils.RequestIdUtils;
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
@Tag(name = "部门管理", description = "B 端部门树与数据范围管理接口")
@RequestMapping("/apis/v1/auth/s/depts")
public class AuthDeptController {

    private final DeptService deptService;

    @Operation(summary = "新增部门")
    @PostMapping
    @RepeatSubmit
    @SaCheckPermission("dept:add")
    public DeptDTO create(@RequestBody @Valid DeptDTO dto) {
        dto.setId(null);
        return deptService.create(dto);
    }

    @Operation(summary = "修改部门")
    @PutMapping("/{id}")
    @RepeatSubmit
    @SaCheckPermission("dept:edit")
    public DeptDTO update(@PathVariable String id, @RequestBody @Valid DeptDTO dto) {
        return deptService.update(RequestIdUtils.parseId(id, "部门ID"), dto);
    }

    @Operation(summary = "删除部门")
    @DeleteMapping("/{id}")
    @RepeatSubmit
    @SaCheckPermission("dept:remove")
    public void delete(@PathVariable String id) {
        deptService.delete(RequestIdUtils.parseId(id, "部门ID"));
    }

    @Operation(summary = "部门详情")
    @GetMapping("/{id}")
    @SaCheckPermission("dept:view")
    public DeptDTO getById(@PathVariable String id) {
        return deptService.getById(RequestIdUtils.parseId(id, "部门ID"));
    }

    @Operation(summary = "分页查询部门")
    @PostMapping("/page")
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
    @SaCheckPermission("dept:status")
    public DeptDTO updateStatus(@PathVariable String id, @RequestBody @Valid StatusUpdateDTO dto) {
        return deptService.updateStatus(RequestIdUtils.parseId(id, "部门ID"), dto.getStatus());
    }
}
