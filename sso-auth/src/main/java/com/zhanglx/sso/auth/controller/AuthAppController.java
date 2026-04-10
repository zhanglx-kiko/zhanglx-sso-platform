package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.AppDTO;
import com.zhanglx.sso.auth.domain.dto.AppQueryDTO;
import com.zhanglx.sso.auth.domain.dto.EnableStatusUpdateDTO;
import com.zhanglx.sso.auth.service.AppService;
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
 * 认证应用控制器。
 */
@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "应用管理", description = "后台应用管理接口")
@RequestMapping("/apis/v1/auth/s/apps")
public class AuthAppController {
    /**
     * 应用服务。
     */
    private final AppService appService;

    @Operation(summary = "新增应用")
    @PostMapping
    @RepeatSubmit
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("app:add")
    @OperationLog(module = "应用管理", feature = "应用", operationType = "CREATE", operationName = "新增应用", operationDesc = "新增接入应用", includeResponseBody = true)
    public AppDTO create(@RequestBody @Valid AppDTO dto) {
        dto.setId(null);
        return appService.create(dto);
    }

    @Operation(summary = "修改应用")
    @PutMapping("/{id}")
    @RepeatSubmit
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("app:edit")
    @OperationLog(module = "应用管理", feature = "应用", operationType = "UPDATE", operationName = "修改应用", operationDesc = "修改接入应用", includeResponseBody = true)
    public AppDTO update(@PathVariable String id, @RequestBody @Valid AppDTO dto) {
        return appService.update(RequestIdUtils.parseId(id, "appId"), dto);
    }

    @Operation(summary = "删除应用")
    @DeleteMapping("/{id}")
    @RepeatSubmit
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("app:remove")
    @OperationLog(module = "应用管理", feature = "应用", operationType = "DELETE", operationName = "删除应用", operationDesc = "删除单个接入应用", includeResponseBody = false)
    public void delete(@PathVariable String id) {
        appService.delete(RequestIdUtils.parseId(id, "appId"));
    }

    @Operation(summary = "批量删除应用")
    @DeleteMapping
    @RepeatSubmit
    @RequestRateLimit(limit = 5, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("app:remove")
    @OperationLog(module = "应用管理", feature = "应用", operationType = "DELETE", operationName = "批量删除应用", operationDesc = "批量删除接入应用", includeResponseBody = false)
    public void batchDelete(@RequestBody List<String> ids) {
        AssertUtils.notEmpty(ids, "应用 ID 列表不能为空");
        appService.batchDelete(RequestIdUtils.parseIds(ids, "appId"));
    }

    @Operation(summary = "应用详情")
    @GetMapping("/{id}")
    @SaCheckPermission("app:view")
    public AppDTO getById(@PathVariable String id) {
        return appService.getById(RequestIdUtils.parseId(id, "appId"));
    }

    @Operation(summary = "分页查询应用")
    @PostMapping("/page")
    @RequestRateLimit(limit = 60, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("app:list")
    public Page<AppDTO> pageQuery(@RequestBody AppQueryDTO queryDTO) {
        return appService.pageQuery(queryDTO);
    }

    @Operation(summary = "更新应用状态")
    @PatchMapping("/{id}/status")
    @RepeatSubmit
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("app:status")
    @OperationLog(module = "应用管理", feature = "应用", operationType = "STATUS", operationName = "修改应用状态", operationDesc = "启停接入应用", includeResponseBody = true)
    public AppDTO updateStatus(@PathVariable String id, @RequestBody @Valid EnableStatusUpdateDTO dto) {
        return appService.updateStatus(RequestIdUtils.parseId(id, "appId"), dto.getStatus());
    }
}