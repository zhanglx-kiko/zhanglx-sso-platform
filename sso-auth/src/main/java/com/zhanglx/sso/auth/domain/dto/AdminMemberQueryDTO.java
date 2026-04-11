package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.auth.enums.MemberTypeEnum;
import com.zhanglx.sso.auth.enums.RealNameStatusEnum;
import com.zhanglx.sso.auth.enums.UserStatusEnum;
import com.zhanglx.sso.auth.enums.YesNoEnum;
import com.zhanglx.sso.core.config.StringToLocalDateTimeDeserializer;
import com.zhanglx.sso.core.config.StringToLongDeserializer;
import com.zhanglx.sso.core.domain.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;

/**
 * 后台会员分页查询条件。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "AdminMemberQueryDTO", description = "后台会员分页查询条件")
public class AdminMemberQueryDTO extends PageQuery {

    /**
     * 会员 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(description = "会员ID")
    private Long memberId;

    /**
     * 手机号。
     */
    @Schema(description = "手机号")
    private String phoneNumber;

    /**
     * 昵称。
     */
    @Schema(description = "昵称")
    private String nickname;

    /**
     * 邮箱。
     */
    @Schema(description = "邮箱")
    private String email;

    /**
     * 会员状态。
     */
    @Schema(description = "会员状态")
    private UserStatusEnum status;

    /**
     * 实名状态。
     */
    @Schema(description = "实名状态")
    private RealNameStatusEnum realNameStatus;

    /**
     * 会员类型。
     */
    @Schema(description = "会员类型")
    private MemberTypeEnum memberType;

    /**
     * 用户等级。
     */
    @Schema(description = "用户等级")
    private Integer userLevel;

    /**
     * 是否已绑定手机号。
     */
    @Schema(description = "是否已绑定手机号")
    private YesNoEnum phoneBound;

    /**
     * 是否已绑定微信。
     */
    @Schema(description = "是否已绑定微信")
    private YesNoEnum hasWechatBind;

    /**
     * 注册开始时间。
     */
    @JsonDeserialize(using = StringToLocalDateTimeDeserializer.class)
    @Schema(description = "注册开始时间")
    private LocalDateTime registerStartTime;

    /**
     * 注册结束时间。
     */
    @JsonDeserialize(using = StringToLocalDateTimeDeserializer.class)
    @Schema(description = "注册结束时间")
    private LocalDateTime registerEndTime;

    /**
     * 最后登录开始时间。
     */
    @JsonDeserialize(using = StringToLocalDateTimeDeserializer.class)
    @Schema(description = "最后登录开始时间")
    private LocalDateTime lastLoginStartTime;

    /**
     * 最后登录结束时间。
     */
    @JsonDeserialize(using = StringToLocalDateTimeDeserializer.class)
    @Schema(description = "最后登录结束时间")
    private LocalDateTime lastLoginEndTime;

    /**
     * 注册 IP。
     */
    @Schema(description = "注册IP")
    private String registerIp;

    /**
     * 最后登录 IP。
     */
    @Schema(description = "最后登录IP")
    private String lastLoginIp;
}