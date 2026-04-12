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
@Schema(name = "PlantUploadImageVO", description = "上传图片结果")
public class PlantUploadImageVO {

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "访问地址")
    private String url;

    @Schema(description = "文件大小")
    private Long size;
}
