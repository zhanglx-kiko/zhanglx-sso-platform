package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.EnableStatusUpdateDTO;
import com.zhanglx.sso.auth.domain.dto.PostDTO;
import com.zhanglx.sso.auth.domain.dto.PostQueryDTO;
import com.zhanglx.sso.auth.service.PostService;
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
@Tag(name = "岗位管理", description = "后台岗位管理接口")
@RequestMapping("/apis/v1/auth/s/posts")
public class AuthPostController {

    private final PostService postService;

    @Operation(summary = "新增岗位")
    @PostMapping
    @RepeatSubmit
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("post:add")
    @OperationLog(module = "岗位管理", feature = "岗位", operationType = "CREATE", operationName = "新增岗位", operationDesc = "新增后台岗位", includeResponseBody = true)
    public PostDTO create(@RequestBody @Valid PostDTO dto) {
        dto.setId(null);
        return postService.create(dto);
    }

    @Operation(summary = "修改岗位")
    @PutMapping("/{id}")
    @RepeatSubmit
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("post:edit")
    @OperationLog(module = "岗位管理", feature = "岗位", operationType = "UPDATE", operationName = "修改岗位", operationDesc = "修改后台岗位", includeResponseBody = true)
    public PostDTO update(@PathVariable String id, @RequestBody @Valid PostDTO dto) {
        return postService.update(RequestIdUtils.parseId(id, "postId"), dto);
    }

    @Operation(summary = "删除岗位")
    @DeleteMapping("/{id}")
    @RepeatSubmit
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("post:remove")
    @OperationLog(module = "岗位管理", feature = "岗位", operationType = "DELETE", operationName = "删除岗位", operationDesc = "删除单个后台岗位", includeResponseBody = false)
    public void delete(@PathVariable String id) {
        postService.delete(RequestIdUtils.parseId(id, "postId"));
    }

    @Operation(summary = "批量删除岗位")
    @DeleteMapping
    @RepeatSubmit
    @RequestRateLimit(limit = 5, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("post:remove")
    @OperationLog(module = "岗位管理", feature = "岗位", operationType = "DELETE", operationName = "批量删除岗位", operationDesc = "批量删除后台岗位", includeResponseBody = false)
    public void batchDelete(@RequestBody List<String> ids) {
        AssertUtils.notEmpty(ids, "岗位 ID 列表不能为空");
        postService.batchDelete(RequestIdUtils.parseIds(ids, "postId"));
    }

    @Operation(summary = "岗位详情")
    @GetMapping("/{id}")
    @SaCheckPermission("post:view")
    public PostDTO getById(@PathVariable String id) {
        return postService.getById(RequestIdUtils.parseId(id, "postId"));
    }

    @Operation(summary = "分页查询岗位")
    @PostMapping("/page")
    @RequestRateLimit(limit = 60, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("post:list")
    public Page<PostDTO> pageQuery(@RequestBody PostQueryDTO queryDTO) {
        return postService.pageQuery(queryDTO);
    }

    @Operation(summary = "更新岗位状态")
    @PatchMapping("/{id}/status")
    @RepeatSubmit
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("post:status")
    @OperationLog(module = "岗位管理", feature = "岗位", operationType = "STATUS", operationName = "修改岗位状态", operationDesc = "启停后台岗位", includeResponseBody = true)
    public PostDTO updateStatus(@PathVariable String id, @RequestBody @Valid EnableStatusUpdateDTO dto) {
        return postService.updateStatus(RequestIdUtils.parseId(id, "postId"), dto.getStatus());
    }
}
