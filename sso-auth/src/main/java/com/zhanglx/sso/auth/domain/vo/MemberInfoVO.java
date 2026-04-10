package com.zhanglx.sso.auth.domain.vo;

import com.zhanglx.sso.auth.enums.GenderEnum;
import com.zhanglx.sso.auth.enums.UserStatusEnum;
import com.zhanglx.sso.core.domain.vo.BaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
/**
 * 会员信息视图对象。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "MemberInfoVO", description = "会员信息")
public class MemberInfoVO extends BaseVO {

    @Schema(description = "手机号", accessMode = Schema.AccessMode.READ_ONLY)
    private String phoneNumber;

    @Schema(description = "昵称", accessMode = Schema.AccessMode.READ_ONLY)
    private String nickname;

    @Schema(description = "头像地址", accessMode = Schema.AccessMode.READ_ONLY)
    private String avatar;

    @Schema(description = "性别", accessMode = Schema.AccessMode.READ_ONLY)
    private GenderEnum sex;

    @Schema(description = "生日", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDate birthday;

    @Schema(description = "邮箱", accessMode = Schema.AccessMode.READ_ONLY)
    private String email;

    @Schema(description = "是否已绑定手机号", accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean phoneBound;

    @Schema(description = "用户等级", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer userLevel;

    @Schema(description = "积分", accessMode = Schema.AccessMode.READ_ONLY)
    private Long points;

    @Schema(description = "会员类型", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer memberType;

    @Schema(description = "实名状态", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer realNameStatus;

    @Schema(description = "状态", accessMode = Schema.AccessMode.READ_ONLY)
    private UserStatusEnum status;

    @Schema(description = "注册 IP", accessMode = Schema.AccessMode.READ_ONLY)
    private String registerIp;

    @Schema(description = "最后登录时间", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime lastLoginTime;

    @Schema(description = "最后登录 IP", accessMode = Schema.AccessMode.READ_ONLY)
    private String lastLoginIp;

    @Schema(description = "扩展资料 JSON", accessMode = Schema.AccessMode.READ_ONLY)
    private String profileExtra;
}
