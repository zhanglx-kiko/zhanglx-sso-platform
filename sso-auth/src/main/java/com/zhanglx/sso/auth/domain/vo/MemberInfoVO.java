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

    /**
     * 手机号。
     */
    @Schema(description = "手机号", accessMode = Schema.AccessMode.READ_ONLY)
    private String phoneNumber;

    /**
     * 昵称。
     */
    @Schema(description = "昵称", accessMode = Schema.AccessMode.READ_ONLY)
    private String nickname;

    /**
     * 头像地址。
     */
    @Schema(description = "头像地址", accessMode = Schema.AccessMode.READ_ONLY)
    private String avatar;

    /**
     * 性别。
     */
    @Schema(description = "性别", accessMode = Schema.AccessMode.READ_ONLY)
    private GenderEnum sex;

    /**
     * 生日。
     */
    @Schema(description = "生日", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDate birthday;

    /**
     * 邮箱。
     */
    @Schema(description = "邮箱", accessMode = Schema.AccessMode.READ_ONLY)
    private String email;

    /**
     * 是否已绑定手机号。
     */
    @Schema(description = "是否已绑定手机号", accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean phoneBound;

    /**
     * 用户等级。
     */
    @Schema(description = "用户等级", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer userLevel;

    /**
     * 积分。
     */
    @Schema(description = "积分", accessMode = Schema.AccessMode.READ_ONLY)
    private Long points;

    /**
     * 会员类型。
     */
    @Schema(description = "会员类型", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer memberType;

    /**
     * 实名状态。
     */
    @Schema(description = "实名状态", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer realNameStatus;

    /**
     * 状态。
     */
    @Schema(description = "状态", accessMode = Schema.AccessMode.READ_ONLY)
    private UserStatusEnum status;

    /**
     * 注册 IP。
     */
    @Schema(description = "注册 IP", accessMode = Schema.AccessMode.READ_ONLY)
    private String registerIp;

    /**
     * 最后登录时间。
     */
    @Schema(description = "最后登录时间", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录 IP。
     */
    @Schema(description = "最后登录 IP", accessMode = Schema.AccessMode.READ_ONLY)
    private String lastLoginIp;

    /**
     * 扩展资料 序列化文本。
     */
    @Schema(description = "扩展资料 JSON", accessMode = Schema.AccessMode.READ_ONLY)
    private String profileExtra;
}