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
@Schema(name = "PlantItemImageVO", description = "花草苗木图片")
public class PlantItemImageVO {

    @Schema(description = "图片地址")
    private String imageUrl;

    @Schema(description = "是否封面")
    private Boolean cover;

    @Schema(description = "排序号")
    private Integer sortNum;
}
