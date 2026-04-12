package com.zhanglx.sso.horticulturalplants.domain.dto;

import com.zhanglx.sso.horticulturalplants.enums.PlantItemPublishStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PlantItemPublishStatusDTO", description = "花草苗木发布状态更新参数")
public class PlantItemPublishStatusDTO {

    @NotNull(message = "发布状态不能为空")
    @Schema(description = "发布状态：0-草稿，1-上架，2-下架")
    private PlantItemPublishStatusEnum publishStatus;
}
