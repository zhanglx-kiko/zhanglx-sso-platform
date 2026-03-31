package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhanglx.sso.auth.domain.dto.PermissionDTO;
import com.zhanglx.sso.auth.domain.dto.excel.ExportProgressDTO;
import com.zhanglx.sso.auth.domain.dto.excel.ImportProgressDTO;
import com.zhanglx.sso.auth.domain.vo.PermissionVO;
import com.zhanglx.sso.auth.service.PermissionService;
import com.zhanglx.sso.auth.utils.excel.ExportProgressManager;
import com.zhanglx.sso.auth.utils.excel.ImportProgressManager;
import com.zhanglx.sso.core.utils.AssertUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/17 11:35
 * @ClassName: PermissionController
 * @Description:
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "权限管理", description = "权限导入与维护")
@RequestMapping("/apis/v1/permissions")
public class PermissionController {

    private final PermissionService permissionService;
    private final ImportProgressManager progressManager;
    private final ExportProgressManager exportProgressManager;

    // ==================== 1. 权限基础管理 ====================

    /**
     * 新增权限项
     * 路径：POST /apis/v1/permissions
     * 权限：permission:add
     */
    @Operation(summary = "新增权限项")
    @PostMapping
    @SaCheckPermission("permission:add")
    public PermissionDTO addPermission(@RequestBody @Valid PermissionDTO permissionDTO) {
        return permissionService.addPermission(permissionDTO);
    }

    /**
     * 修改权限项
     * 路径：PUT /apis/v1/permissions/{id}
     * 权限：permission:edit
     */
    @Operation(summary = "修改权限项")
    @PutMapping("/{id}")
    @SaCheckPermission("permission:edit")
    @Parameter(name = "id", description = "权限项 ID", required = true, in = ParameterIn.PATH)
    public PermissionDTO updatePermission(@PathVariable Long id, @RequestBody @Valid PermissionDTO permissionDTO) {
        return permissionService.updatePermission(id, permissionDTO);
    }

    /**
     * 删除权限项（单个）
     * 路径：DELETE /apis/v1/permissions/{id}
     * 权限：permission:remove
     */
    @Operation(summary = "删除权限项")
    @DeleteMapping("/{id}")
    @SaCheckPermission("permission:remove")
    @Parameter(name = "id", description = "权限项 ID", required = true, in = ParameterIn.PATH)
    public PermissionDTO delPermission(@PathVariable Long id) {
        return permissionService.delPermission(id);
    }

    /**
     * 批量删除权限项
     * 路径：DELETE /apis/v1/permissions/batch
     * 权限：permission:remove
     */
    @Operation(summary = "批量删除权限项")
    @DeleteMapping("/batch")
    @SaCheckPermission("permission:remove")
    public List<PermissionDTO> batchDelPermission(@RequestBody List<Long> idList) {
        AssertUtils.notEmpty(idList, "权限项 ID 列表不能为空");
        return permissionService.batchDelPermission(idList);
    }

    /**
     * 查询权限树形列表
     * 路径：GET /apis/v1/permissions/tree
     * 权限：permission:list
     */
    @Operation(summary = "查询权限树形列表")
    @GetMapping("/tree")
    @SaCheckPermission("permission:list")
    @Parameter(name = "searchKey", description = "搜索关键字（支持名称或标识）", required = false, in = ParameterIn.QUERY)
    public List<PermissionDTO> getPermissionTree(@RequestParam(required = false, defaultValue = "") String searchKey) {
        return permissionService.selPermission(searchKey);
    }

    /**
     * 根据标识查询权限列表
     * 路径：POST /apis/v1/permissions/by-identification
     * 权限：permission:list
     */
    @Operation(summary = "根据标识查询权限列表")
    @PostMapping("/by-identification")
    @SaCheckPermission("permission:list")
    public List<PermissionVO> getPermissionsByIdentification(
            @RequestParam String username,
            @RequestBody(required = false) List<String> identifications,
            @RequestBody(required = false) List<String> permissionTypes,
            @RequestParam(required = false) String tenantId
    ) {
        return permissionService.selPermissionByIdentification(username, identifications, permissionTypes, tenantId);
    }

    @Operation(summary = "异步批量导入权限 (尽力而为)")
    @PostMapping("/import")
    public String importPermissions(@RequestParam("file") MultipartFile file) throws Exception {
        // 1. 生成唯一任务 ID
        String taskId = UUID.randomUUID().toString().replace("-", "");

        // 2. 初始化 Redis 进度
        progressManager.initTask(taskId);

        // 3. 【核心优化】：避免 OOM，直接落盘为系统临时文件
        // 创建一个前缀为 upload_import_，后缀为 .xlsx 的临时文件
        File tempFile = File.createTempFile("upload_import_" + taskId, ".xlsx");
        file.transferTo(tempFile);

        // 4. 交给虚拟线程异步处理，传入临时文件对象
        permissionService.executeImportTask(taskId, tempFile);

        // 5. 立刻返回任务 ID 给前端
        return taskId;
    }

    @Operation(summary = "查询导入进度")
    @GetMapping("/import/progress/{taskId}")
    public ImportProgressDTO getImportProgress(@PathVariable("taskId") String taskId) {
        ImportProgressDTO progress = progressManager.getProgress(taskId);
        AssertUtils.notNull(progress, "任务不存在或已过期");

        return progress;
    }

    @Operation(summary = "异步导出权限数据")
    @GetMapping("/export")
    public String exportPermissions() {
        // 1. 生成唯一任务 ID
        String taskId = UUID.randomUUID().toString().replace("-", "");

        // 2. 初始化 Redis 进度 (复用之前的逻辑)
        progressManager.initTask(taskId);

        // 3. 交给虚拟线程异步处理
        permissionService.executeExportTask(taskId);

        // 4. 立刻返回任务 ID 给前端，前端使用同一个进度查询接口进行轮询
        return taskId;
    }

    @Operation(summary = "查询导出进度")
    @GetMapping("/export/progress/{taskId}")
    public ExportProgressDTO getExportProgress(@PathVariable("taskId") String taskId) {
        ExportProgressDTO progress = exportProgressManager.getProgress(taskId);
        AssertUtils.notNull(progress, "导出任务不存在或已过期");
        return progress;
    }

}
