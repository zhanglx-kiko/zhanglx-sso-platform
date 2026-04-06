package com.zhanglx.sso.auth.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * @Author: Zhang L X
 * @Create: 2026/4/3 12:19
 * @ClassName: UserInfoDTO
 * @Description:
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "UserInfoDTO", description = "用户信息返回对象")
public class UserInfoDTO extends UserBaseDTO {

    @Schema(description = "账号", accessMode = Schema.AccessMode.READ_ONLY)
    private String username;

    @Schema(description = "状态（0禁用 1正常）", accessMode = Schema.AccessMode.READ_ONLY, allowableValues = {"0", "1"})
    private Integer status;

    @Schema(description = "用户等级", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer userLevel;

    @Schema(description = "积分", accessMode = Schema.AccessMode.READ_ONLY)
    private Long points;

    @Schema(description = "会员类型", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer memberType;

    @Schema(description = "实名认证状态", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer realNameStatus;

    @Schema(description = "最后登录时间", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime lastLoginTime;

    @Schema(description = "最后登录IP", name = "lastLoginIp", accessMode = Schema.AccessMode.READ_ONLY)
    private String lastLoginIp;

}
