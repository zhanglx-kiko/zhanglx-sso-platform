package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.ConfigDTO;
import com.zhanglx.sso.auth.domain.dto.ConfigQueryDTO;
import com.zhanglx.sso.auth.service.ConfigService;
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

/**
 * 认证配置控制器。
 */
@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "参数管理", description = "B 端系统参数管理接口")
@RequestMapping("/apis/v1/auth/s/configs")
public class AuthConfigController {
    /**
     * 参数配置服务。
     */
    private final ConfigService configService;

    @Operation(summary = "新增参数")
    @PostMapping
    @RepeatSubmit
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("config:add")
    @OperationLog(module = "参数管理", feature = "系统参数", operationType = "CREATE", operationName = "新增参数", operationDesc = "新增系统参数", includeResponseBody = true)
    public ConfigDTO create(@RequestBody @Valid ConfigDTO dto) {
        dto.setId(null);
        return configService.create(dto);
    }

    @Operation(summary = "修改参数")
    @PutMapping("/{id}")
    @RepeatSubmit
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("config:edit")
    @OperationLog(module = "参数管理", feature = "系统参数", operationType = "UPDATE", operationName = "修改参数", operationDesc = "修改系统参数", includeResponseBody = true)
    public ConfigDTO update(@PathVariable String id, @RequestBody @Valid ConfigDTO dto) {
        return configService.update(RequestIdUtils.parseId(id, "参数ID"), dto);
    }

    @Operation(summary = "删除参数")
    @DeleteMapping("/{id}")
    @RepeatSubmit
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("config:remove")
    @OperationLog(module = "参数管理", feature = "系统参数", operationType = "DELETE", operationName = "删除参数", operationDesc = "删除系统参数", includeResponseBody = false)
    public void delete(@PathVariable String id) {
        configService.delete(RequestIdUtils.parseId(id, "参数ID"));
    }

    @Operation(summary = "参数详情")
    @GetMapping("/{id}")
    @SaCheckPermission("config:view")
    public ConfigDTO getById(@PathVariable String id) {
        return configService.getById(RequestIdUtils.parseId(id, "参数ID"));
    }

    @Operation(summary = "按键查询参数")
    @GetMapping("/by-key/{configKey}")
    @SaCheckPermission("config:view")
    public ConfigDTO getByKey(@PathVariable String configKey) {
        return configService.getByKey(configKey);
    }

    @Operation(summary = "分页查询参数")
    @PostMapping("/page")
    @RequestRateLimit(limit = 60, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("config:list")
    public Page<ConfigDTO> pageQuery(@RequestBody ConfigQueryDTO queryDTO) {
        return configService.pageQuery(queryDTO);
    }
}