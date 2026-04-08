package com.zhanglx.sso.auth.domain.vo;

import com.zhanglx.sso.auth.enums.UserStatusEnum;
import com.zhanglx.sso.core.domain.vo.BaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "MemberInfoVO", description = "会员信息")
public class MemberInfoVO extends BaseVO {

    @Schema(description = "手机号", accessMode = Schema.AccessMode.READ_ONLY)
    private String phoneNumber;

    @Schema(description = "是否已绑定手机号", accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean phoneBound;

    @Schema(description = "状态", accessMode = Schema.AccessMode.READ_ONLY)
    private UserStatusEnum status;

    @Schema(description = "注册 IP", accessMode = Schema.AccessMode.READ_ONLY)
    private String registerIp;

    @Schema(description = "最后登录时间", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime lastLoginTime;
}
