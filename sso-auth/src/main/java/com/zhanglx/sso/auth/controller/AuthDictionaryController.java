package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.DictDataDTO;
import com.zhanglx.sso.auth.domain.dto.DictDataQueryDTO;
import com.zhanglx.sso.auth.domain.dto.DictTypeDTO;
import com.zhanglx.sso.auth.domain.dto.DictTypeQueryDTO;
import com.zhanglx.sso.auth.domain.dto.EnableStatusUpdateDTO;
import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.auth.service.DictionaryService;
import com.zhanglx.sso.auth.utils.RequestIdUtils;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "字典管理", description = "后台字典类型与字典数据管理接口")
@RequestMapping("/apis/v1/auth/s/dicts")
public class AuthDictionaryController {

    private final DictionaryService dictionaryService;

    @Operation(summary = "新增字典类型")
    @PostMapping("/types")
    @RepeatSubmit
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("dict:type:add")
    public DictTypeDTO createType(@RequestBody @Valid DictTypeDTO dto) {
        dto.setId(null);
        return dictionaryService.createType(dto);
    }

    @Operation(summary = "修改字典类型")
    @PutMapping("/types/{id}")
    @RepeatSubmit
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("dict:type:edit")
    public DictTypeDTO updateType(@PathVariable String id, @RequestBody @Valid DictTypeDTO dto) {
        return dictionaryService.updateType(RequestIdUtils.parseId(id, "dictTypeId"), dto);
    }

    @Operation(summary = "删除字典类型")
    @DeleteMapping("/types/{id}")
    @RepeatSubmit
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("dict:type:remove")
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
    public DictTypeDTO updateTypeStatus(@PathVariable String id, @RequestBody @Valid EnableStatusUpdateDTO dto) {
        return dictionaryService.updateTypeStatus(RequestIdUtils.parseId(id, "dictTypeId"), dto.getStatus());
    }

    @Operation(summary = "新增字典数据")
    @PostMapping("/data")
    @RepeatSubmit
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("dict:data:add")
    public DictDataDTO createData(@RequestBody @Valid DictDataDTO dto) {
        dto.setId(null);
        return dictionaryService.createData(dto);
    }

    @Operation(summary = "修改字典数据")
    @PutMapping("/data/{id}")
    @RepeatSubmit
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("dict:data:edit")
    public DictDataDTO updateData(@PathVariable String id, @RequestBody @Valid DictDataDTO dto) {
        return dictionaryService.updateData(RequestIdUtils.parseId(id, "dictDataId"), dto);
    }

    @Operation(summary = "删除字典数据")
    @DeleteMapping("/data/{id}")
    @RepeatSubmit
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("dict:data:remove")
    public void deleteData(@PathVariable String id) {
        dictionaryService.deleteData(RequestIdUtils.parseId(id, "dictDataId"));
    }

    @Operation(summary = "字典数据详情")
    @GetMapping("/data/{id}")
    @SaCheckPermission("dict:data:view")
    public DictDataDTO getData(@PathVariable String id) {
        return dictionaryService.getData(RequestIdUtils.parseId(id, "dictDataId"));
    }

    @Operation(summary = "分页查询字典数据")
    @PostMapping("/data/page")
    @RequestRateLimit(limit = 60, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("dict:data:list")
    public Page<DictDataDTO> pageData(@RequestBody DictDataQueryDTO queryDTO) {
        return dictionaryService.pageData(queryDTO);
    }

    @Operation(summary = "按字典类型查询字典数据")
    @GetMapping("/data/by-type/{dictType}")
    @SaCheckPermission("dict:data:list")
    public List<DictDataDTO> listDataByType(@PathVariable String dictType,
                                            @RequestParam(required = false) Integer status) {
        return dictionaryService.listDataByType(dictType, EnableStatusEnum.fromCode(status));
    }

    @Operation(summary = "更新字典数据状态")
    @PatchMapping("/data/{id}/status")
    @RepeatSubmit
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @SaCheckPermission("dict:data:status")
    public DictDataDTO updateDataStatus(@PathVariable String id, @RequestBody @Valid EnableStatusUpdateDTO dto) {
        return dictionaryService.updateDataStatus(RequestIdUtils.parseId(id, "dictDataId"), dto.getStatus());
    }
}