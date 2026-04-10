package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.*;
import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.auth.service.DictionaryService;
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
 * 认证字典控制器。
 */
@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "字典管理", description = "后台字典类型与DictData管理接口")
@RequestMapping("/apis/v1/auth/s/dicts")
public class AuthDictionaryController {
    /**
     * 字典服务。
     */
    private final DictionaryService dictionaryService;

    @Operation(summary = "新增字典类型")
    @PostMapping("/types")
    @RepeatSubmit
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("dict:type:add")
    @OperationLog(module = "字典管理", feature = "字典类型", operationType = "CREATE", operationName = "新增字典类型", operationDesc = "新增系统字典类型", includeResponseBody = true)
    public DictTypeDTO createType(@RequestBody @Valid DictTypeDTO dto) {
        dto.setId(null);
        return dictionaryService.createType(dto);
    }

    @Operation(summary = "修改字典类型")
    @PutMapping("/types/{id}")
    @RepeatSubmit
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("dict:type:edit")
    @OperationLog(module = "字典管理", feature = "字典类型", operationType = "UPDATE", operationName = "修改字典类型", operationDesc = "修改系统字典类型", includeResponseBody = true)
    public DictTypeDTO updateType(@PathVariable String id, @RequestBody @Valid DictTypeDTO dto) {
        return dictionaryService.updateType(RequestIdUtils.parseId(id, "dictTypeId"), dto);
    }

    @Operation(summary = "删除字典类型")
    @DeleteMapping("/types/{id}")
    @RepeatSubmit
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("dict:type:remove")
    @OperationLog(module = "字典管理", feature = "字典类型", operationType = "DELETE", operationName = "删除字典类型", operationDesc = "删除系统字典类型", includeResponseBody = false)
    public void deleteType(@PathVariable String id) {
        dictionaryService.deleteType(RequestIdUtils.parseId(id, "dictTypeId"));
    }

    @Operation(summary = "字典类型详情")
    @GetMapping("/types/{id}")
    @SaCheckPermission("dict:type:view")
    public DictTypeDTO getType(@PathVariable String id) {
        return dictionaryService.getType(RequestIdUtils.parseId(id, "dictTypeId"));
    }

    @Operation(summary = "分页查询字典类型")
    @PostMapping("/types/page")
    @RequestRateLimit(limit = 60, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("dict:type:list")
    public Page<DictTypeDTO> pageType(@RequestBody DictTypeQueryDTO queryDTO) {
        return dictionaryService.pageType(queryDTO);
    }

    @Operation(summary = "更新字典类型状态")
    @PatchMapping("/types/{id}/status")
    @RepeatSubmit
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("dict:type:status")
    @OperationLog(module = "字典管理", feature = "字典类型", operationType = "STATUS", operationName = "修改字典类型状态", operationDesc = "启停系统字典类型", includeResponseBody = true)
    public DictTypeDTO updateTypeStatus(@PathVariable String id, @RequestBody @Valid EnableStatusUpdateDTO dto) {
        return dictionaryService.updateTypeStatus(RequestIdUtils.parseId(id, "dictTypeId"), dto.getStatus());
    }

    @Operation(summary = "新增DictData")
    @PostMapping("/data")
    @RepeatSubmit
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("dict:data:add")
    @OperationLog(module = "字典管理", feature = "DictData", operationType = "CREATE", operationName = "新增DictData", operationDesc = "新增系统DictData", includeResponseBody = true)
    public DictDataDTO createData(@RequestBody @Valid DictDataDTO dto) {
        dto.setId(null);
        return dictionaryService.createData(dto);
    }

    @Operation(summary = "修改DictData")
    @PutMapping("/data/{id}")
    @RepeatSubmit
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("dict:data:edit")
    @OperationLog(module = "字典管理", feature = "DictData", operationType = "UPDATE", operationName = "修改DictData", operationDesc = "修改系统DictData", includeResponseBody = true)
    public DictDataDTO updateData(@PathVariable String id, @RequestBody @Valid DictDataDTO dto) {
        return dictionaryService.updateData(RequestIdUtils.parseId(id, "dictDataId"), dto);
    }

    @Operation(summary = "删除DictData")
    @DeleteMapping("/data/{id}")
    @RepeatSubmit
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("dict:data:remove")
    @OperationLog(module = "字典管理", feature = "DictData", operationType = "DELETE", operationName = "删除DictData", operationDesc = "删除系统DictData", includeResponseBody = false)
    public void deleteData(@PathVariable String id) {
        dictionaryService.deleteData(RequestIdUtils.parseId(id, "dictDataId"));
    }

    @Operation(summary = "DictData详情")
    @GetMapping("/data/{id}")
    @SaCheckPermission("dict:data:view")
    public DictDataDTO getData(@PathVariable String id) {
        return dictionaryService.getData(RequestIdUtils.parseId(id, "dictDataId"));
    }

    @Operation(summary = "分页查询DictData")
    @PostMapping("/data/page")
    @RequestRateLimit(limit = 60, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("dict:data:list")
    public Page<DictDataDTO> pageData(@RequestBody DictDataQueryDTO queryDTO) {
        return dictionaryService.pageData(queryDTO);
    }

    @Operation(summary = "按字典类型查询DictData")
    @GetMapping("/data/by-type/{dictType}")
    @SaCheckPermission("dict:data:list")
    public List<DictDataDTO> listDataByType(@PathVariable String dictType,
                                            @RequestParam(required = false) Integer status) {
        return dictionaryService.listDataByType(dictType, EnableStatusEnum.fromCode(status));
    }

    @Operation(summary = "更新DictData状态")
    @PatchMapping("/data/{id}/status")
    @RepeatSubmit
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("dict:data:status")
    @OperationLog(module = "字典管理", feature = "DictData", operationType = "STATUS", operationName = "修改DictData状态", operationDesc = "启停系统DictData", includeResponseBody = true)
    public DictDataDTO updateDataStatus(@PathVariable String id, @RequestBody @Valid EnableStatusUpdateDTO dto) {
        return dictionaryService.updateDataStatus(RequestIdUtils.parseId(id, "dictDataId"), dto.getStatus());
    }
}