package com.zhanglx.sso.horticulturalplants.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.core.utils.satoken.StpMemberUtil;
import com.zhanglx.sso.horticulturalplants.domain.dto.PlantItemPageQueryDTO;
import com.zhanglx.sso.horticulturalplants.domain.dto.PlantItemPublishStatusDTO;
import com.zhanglx.sso.horticulturalplants.domain.dto.PlantItemSaveDTO;
import com.zhanglx.sso.horticulturalplants.domain.vo.PlantCategoryVO;
import com.zhanglx.sso.horticulturalplants.domain.vo.PlantItemCardVO;
import com.zhanglx.sso.horticulturalplants.domain.vo.PlantItemDetailVO;
import com.zhanglx.sso.horticulturalplants.domain.vo.PlantMineSummaryVO;
import com.zhanglx.sso.horticulturalplants.domain.vo.PlantUploadImageVO;
import com.zhanglx.sso.horticulturalplants.service.PlantAssetService;
import com.zhanglx.sso.horticulturalplants.service.PlantItemService;
import com.zhanglx.sso.log.annotation.OperationLog;
import com.zhanglx.sso.web.annotation.RateLimitDimension;
import com.zhanglx.sso.web.annotation.RepeatSubmit;
import com.zhanglx.sso.web.annotation.RequestRateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "花草苗木会员内容接口")
public class HorticulturalMemberPlantItemController {

    private final PlantItemService plantItemService;

    private final PlantAssetService plantAssetService;

    @Operation(summary = "查询花草苗木分类")
    @GetMapping("/apis/v1/horticultural-plants/m/categories")
    @RequestRateLimit(limit = 60, windowSeconds = 60, dimensions = {RateLimitDimension.IP, RateLimitDimension.URI})
    public List<PlantCategoryVO> listCategories() {
        return plantItemService.listCategories();
    }

    @Operation(summary = "分页查询花草苗木列表")
    @GetMapping("/apis/v1/horticultural-plants/m/items")
    @RequestRateLimit(limit = 120, windowSeconds = 60, dimensions = {RateLimitDimension.IP, RateLimitDimension.URI})
    public Page<PlantItemCardVO> pageItems(@Validated PlantItemPageQueryDTO queryDTO) {
        return plantItemService.pagePublishedItems(queryDTO);
    }

    @Operation(summary = "查询花草苗木详情")
    @GetMapping("/apis/v1/horticultural-plants/m/items/{itemId}")
    @RequestRateLimit(limit = 120, windowSeconds = 60, dimensions = {RateLimitDimension.IP, RateLimitDimension.URI})
    public PlantItemDetailVO detail(@PathVariable Long itemId) {
        return plantItemService.getItemDetail(itemId, resolveCurrentMemberIdSafely());
    }

    @Operation(summary = "分页查询我的发布")
    @GetMapping("/apis/v1/horticultural-plants/m/items/mine")
    @SaCheckLogin(type = StpMemberUtil.TYPE)
    @RequestRateLimit(limit = 60, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    public Page<PlantItemCardVO> pageMyItems(@Validated PlantItemPageQueryDTO queryDTO) {
        return plantItemService.pageMyItems(StpMemberUtil.getLoginIdAsLong(), queryDTO);
    }

    @Operation(summary = "查询我的发布概览")
    @GetMapping("/apis/v1/horticultural-plants/m/items/mine/summary")
    @SaCheckLogin(type = StpMemberUtil.TYPE)
    @RequestRateLimit(limit = 60, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    public PlantMineSummaryVO mySummary() {
        return plantItemService.getMySummary(StpMemberUtil.getLoginIdAsLong());
    }

    @Operation(summary = "上传花草苗木图片")
    @PostMapping(value = "/apis/v1/horticultural-plants/m/items/upload-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SaCheckLogin(type = StpMemberUtil.TYPE)
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @OperationLog(module = "花草苗木", feature = "图片", operationType = "UPLOAD", operationName = "上传花草苗木图片", operationDesc = "会员上传花草苗木图片素材", includeRequestBody = false)
    public List<PlantUploadImageVO> uploadImages(@RequestPart("files") MultipartFile[] files) {
        return plantAssetService.uploadImages(StpMemberUtil.getLoginIdAsLong(), Arrays.asList(files));
    }

    @Operation(summary = "发布花草苗木内容")
    @PostMapping("/apis/v1/horticultural-plants/m/items")
    @RepeatSubmit
    @SaCheckLogin(type = StpMemberUtil.TYPE)
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @OperationLog(module = "花草苗木", feature = "发布", operationType = "CREATE", operationName = "发布花草苗木内容", operationDesc = "会员发布新的花草苗木建议零售价内容")
    public PlantItemDetailVO create(@RequestBody @Valid PlantItemSaveDTO saveDTO) {
        return plantItemService.createItem(StpMemberUtil.getLoginIdAsLong(), saveDTO);
    }

    @Operation(summary = "编辑花草苗木内容")
    @PutMapping("/apis/v1/horticultural-plants/m/items/{itemId}")
    @RepeatSubmit
    @SaCheckLogin(type = StpMemberUtil.TYPE)
    @RequestRateLimit(limit = 10, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @OperationLog(module = "花草苗木", feature = "编辑", operationType = "UPDATE", operationName = "编辑花草苗木内容", operationDesc = "会员编辑自己发布的花草苗木内容")
    public PlantItemDetailVO update(@PathVariable Long itemId, @RequestBody @Valid PlantItemSaveDTO saveDTO) {
        return plantItemService.updateItem(StpMemberUtil.getLoginIdAsLong(), itemId, saveDTO);
    }

    @Operation(summary = "更新花草苗木上下架状态")
    @PatchMapping("/apis/v1/horticultural-plants/m/items/{itemId}/publish-status")
    @RepeatSubmit
    @SaCheckLogin(type = StpMemberUtil.TYPE)
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @OperationLog(module = "花草苗木", feature = "状态", operationType = "UPDATE", operationName = "更新花草苗木上下架状态", operationDesc = "会员调整自己发布内容的上下架状态", includeResponseBody = false)
    public void updatePublishStatus(@PathVariable Long itemId, @RequestBody @Valid PlantItemPublishStatusDTO dto) {
        plantItemService.updatePublishStatus(StpMemberUtil.getLoginIdAsLong(), itemId, dto.getPublishStatus());
    }

    @Operation(summary = "删除花草苗木内容")
    @DeleteMapping("/apis/v1/horticultural-plants/m/items/{itemId}")
    @RepeatSubmit
    @SaCheckLogin(type = StpMemberUtil.TYPE)
    @RequestRateLimit(limit = 20, windowSeconds = 60, dimensions = {RateLimitDimension.USER_ID, RateLimitDimension.URI})
    @OperationLog(module = "花草苗木", feature = "删除", operationType = "DELETE", operationName = "删除花草苗木内容", operationDesc = "会员软删除自己发布的花草苗木内容", includeResponseBody = false)
    public void delete(@PathVariable Long itemId) {
        plantItemService.deleteItem(StpMemberUtil.getLoginIdAsLong(), itemId);
    }

    private Long resolveCurrentMemberIdSafely() {
        try {
            return StpMemberUtil.isLogin() ? StpMemberUtil.getLoginIdAsLong() : null;
        } catch (Exception e) {
            log.debug("忽略无效会员登录态: {}", e.getMessage(), e);
            return null;
        }
    }
}
