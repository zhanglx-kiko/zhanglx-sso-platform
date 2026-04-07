package com.zhanglx.sso.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.annotation.RepeatSubmit;
import com.zhanglx.sso.auth.domain.dto.*;
import com.zhanglx.sso.auth.service.DictionaryService;
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
@Tag(name = "字典管理", description = "B 端字典类型与字典数据管理接口")
@RequestMapping("/apis/v1/auth/s/dicts")
public class AuthDictionaryController {

    private final DictionaryService dictionaryService;

    @Operation(summary = "新增字典类型")
    @PostMapping("/types")
    @RepeatSubmit
    @SaCheckPermission("dict:type:add")
    public DictTypeDTO createType(@RequestBody @Valid DictTypeDTO dto) {
        dto.setId(null);
        return dictionaryService.createType(dto);
    }

    @Operation(summary = "修改字典类型")
    @PutMapping("/types/{id}")
    @RepeatSubmit
    @SaCheckPermission("dict:type:edit")
    public DictTypeDTO updateType(@PathVariable String id, @RequestBody @Valid DictTypeDTO dto) {
        return dictionaryService.updateType(RequestIdUtils.parseId(id, "字典类型ID"), dto);
    }

    @Operation(summary = "删除字典类型")
    @DeleteMapping("/types/{id}")
    @RepeatSubmit
    @SaCheckPermission("dict:type:remove")
    public void deleteType(@PathVariable String id) {
        dictionaryService.deleteType(RequestIdUtils.parseId(id, "字典类型ID"));
    }

    @Operation(summary = "字典类型详情")
    @GetMapping("/types/{id}")
    @SaCheckPermission("dict:type:view")
    public DictTypeDTO getType(@PathVariable String id) {
        return dictionaryService.getType(RequestIdUtils.parseId(id, "字典类型ID"));
    }

    @Operation(summary = "分页查询字典类型")
    @PostMapping("/types/page")
    @SaCheckPermission("dict:type:list")
    public Page<DictTypeDTO> pageType(@RequestBody DictTypeQueryDTO queryDTO) {
        return dictionaryService.pageType(queryDTO);
    }

    @Operation(summary = "更新字典类型状态")
    @PatchMapping("/types/{id}/status")
    @RepeatSubmit
    @SaCheckPermission("dict:type:status")
    public DictTypeDTO updateTypeStatus(@PathVariable String id, @RequestBody @Valid StatusUpdateDTO dto) {
        return dictionaryService.updateTypeStatus(RequestIdUtils.parseId(id, "字典类型ID"), dto.getStatus());
    }

    @Operation(summary = "新增字典数据")
    @PostMapping("/data")
    @RepeatSubmit
    @SaCheckPermission("dict:data:add")
    public DictDataDTO createData(@RequestBody @Valid DictDataDTO dto) {
        dto.setId(null);
        return dictionaryService.createData(dto);
    }

    @Operation(summary = "修改字典数据")
    @PutMapping("/data/{id}")
    @RepeatSubmit
    @SaCheckPermission("dict:data:edit")
    public DictDataDTO updateData(@PathVariable String id, @RequestBody @Valid DictDataDTO dto) {
        return dictionaryService.updateData(RequestIdUtils.parseId(id, "字典数据ID"), dto);
    }

    @Operation(summary = "删除字典数据")
    @DeleteMapping("/data/{id}")
    @RepeatSubmit
    @SaCheckPermission("dict:data:remove")
    public void deleteData(@PathVariable String id) {
        dictionaryService.deleteData(RequestIdUtils.parseId(id, "字典数据ID"));
    }

    @Operation(summary = "字典数据详情")
    @GetMapping("/data/{id}")
    @SaCheckPermission("dict:data:view")
    public DictDataDTO getData(@PathVariable String id) {
        return dictionaryService.getData(RequestIdUtils.parseId(id, "字典数据ID"));
    }

    @Operation(summary = "分页查询字典数据")
    @PostMapping("/data/page")
    @SaCheckPermission("dict:data:list")
    public Page<DictDataDTO> pageData(@RequestBody DictDataQueryDTO queryDTO) {
        return dictionaryService.pageData(queryDTO);
    }

    @Operation(summary = "按字典类型查询字典数据")
    @GetMapping("/data/by-type/{dictType}")
    @SaCheckPermission("dict:data:list")
    public List<DictDataDTO> listDataByType(@PathVariable String dictType,
                                            @RequestParam(required = false) Integer status) {
        return dictionaryService.listDataByType(dictType, status);
    }

    @Operation(summary = "更新字典数据状态")
    @PatchMapping("/data/{id}/status")
    @RepeatSubmit
    @SaCheckPermission("dict:data:status")
    public DictDataDTO updateDataStatus(@PathVariable String id, @RequestBody @Valid StatusUpdateDTO dto) {
        return dictionaryService.updateDataStatus(RequestIdUtils.parseId(id, "字典数据ID"), dto.getStatus());
    }
}
