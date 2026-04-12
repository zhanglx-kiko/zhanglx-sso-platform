package com.zhanglx.sso.horticulturalplants.domain.vo;

import com.zhanglx.sso.core.domain.vo.BaseVO;
import com.zhanglx.sso.horticulturalplants.enums.PlantItemPublishStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "PlantItemDetailVO", description = "花草苗木详情")
public class PlantItemDetailVO extends BaseVO {

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "花草苗木名称")
    private String title;

    @Schema(description = "封面图")
    private String coverImageUrl;

    @Schema(description = "建议零售价")
    private BigDecimal suggestedRetailPrice;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "简介")
    private String shortDescription;

    @Schema(description = "详情描述")
    private String detailDescription;

    @Schema(description = "省份")
    private String province;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "区县")
    private String area;

    @Schema(description = "发布状态")
    private PlantItemPublishStatusEnum publishStatus;

    @Schema(description = "浏览量")
    private Long viewCount;

    @Schema(description = "图片列表")
    private List<PlantItemImageVO> imageList;

    @Schema(description = "发布人信息")
    private PlantPublisherVO publisher;
}
