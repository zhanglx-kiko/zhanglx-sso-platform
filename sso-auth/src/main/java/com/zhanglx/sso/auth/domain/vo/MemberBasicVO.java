package com.zhanglx.sso.auth.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "MemberBasicVO", description = "会员基础信息")
public class MemberBasicVO {

    @Schema(description = "会员 ID")
    private Long id;

    @Schema(description = "手机号")
    private String phoneNumber;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "是否已绑定手机号")
    private Boolean phoneBound;

    @Schema(description = "会员类型编码")
    private Integer memberType;

    @Schema(description = "实名状态编码")
    private Integer realNameStatus;

    @Schema(description = "会员状态编码")
    private Integer status;
}
