package com.zhanglx.sso.horticulturalplants.domain.dto;

import com.zhanglx.sso.core.config.StringToLongDeserializer;
import com.zhanglx.sso.horticulturalplants.enums.PlantItemPublishStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PlantItemSaveDTO", description = "花草苗木发布/编辑参数")
public class PlantItemSaveDTO {

    @NotNull(message = "分类不能为空")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(description = "分类ID")
    private Long categoryId;

    @NotBlank(message = "花草苗木名称不能为空")
    @Size(max = 64, message = "花草苗木名称长度不能超过64个字符")
    @Schema(description = "花草苗木名称")
    private String title;

    @NotNull(message = "建议零售价不能为空")
    @DecimalMin(value = "0.01", message = "建议零售价必须大于0")
    @Schema(description = "建议零售价")
    private BigDecimal suggestedRetailPrice;

    @NotBlank(message = "价格单位不能为空")
    @Size(max = 16, message = "价格单位长度不能超过16个字符")
    @Schema(description = "价格单位")
    private String unit;

    @NotBlank(message = "简介不能为空")
    @Size(max = 200, message = "简介长度不能超过200个字符")
    @Schema(description = "简介")
    private String shortDescription;

    @NotBlank(message = "详情描述不能为空")
    @Size(max = 5000, message = "详情描述长度不能超过5000个字符")
    @Schema(description = "详情描述")
    private String detailDescription;

    @NotEmpty(message = "请至少上传一张图片")
    @Size(max = 9, message = "最多上传9张图片")
    @Schema(description = "图片地址列表")
    private List<String> imageUrls;

    @Schema(description = "封面图地址，留空时默认使用第一张图片")
    private String coverImageUrl;

    @Size(max = 32, message = "省份长度不能超过32个字符")
    @Schema(description = "省份")
    private String province;

    @Size(max = 32, message = "城市长度不能超过32个字符")
    @Schema(description = "城市")
    private String city;

    @Size(max = 32, message = "区县长度不能超过32个字符")
    @Schema(description = "区县")
    private String area;

    @NotNull(message = "发布状态不能为空")
    @Schema(description = "发布状态：0-草稿，1-上架，2-下架")
    private PlantItemPublishStatusEnum publishStatus;
}
