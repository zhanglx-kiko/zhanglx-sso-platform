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
@Schema(name = "PlantPublisherVO", description = "发布人信息")
public class PlantPublisherVO {

    @Schema(description = "发布人ID")
    private Long id;

    @Schema(description = "发布人昵称")
    private String nickname;

    @Schema(description = "发布人头像")
    private String avatar;
}
