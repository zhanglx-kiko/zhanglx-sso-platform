package com.zhanglx.sso.horticulturalplants.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PlantMineSummaryVO", description = "我的发布概览")
public class PlantMineSummaryVO {

    @Schema(description = "总数")
    private Long totalCount;

    @Schema(description = "上架数")
    private Long publishedCount;

    @Schema(description = "草稿数")
    private Long draftCount;

    @Schema(description = "下架数")
    private Long offShelfCount;
}
