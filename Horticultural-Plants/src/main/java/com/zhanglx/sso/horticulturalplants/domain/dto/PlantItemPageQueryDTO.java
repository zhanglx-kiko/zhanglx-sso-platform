package com.zhanglx.sso.horticulturalplants.domain.dto;

import com.zhanglx.sso.core.domain.page.PageQuery;
import com.zhanglx.sso.horticulturalplants.enums.PlantItemPublishStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "PlantItemPageQueryDTO", description = "花草苗木分页查询条件")
public class PlantItemPageQueryDTO extends PageQuery {

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "省份")
    private String province;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "发布状态，仅我的发布列表使用")
    private PlantItemPublishStatusEnum publishStatus;
}
