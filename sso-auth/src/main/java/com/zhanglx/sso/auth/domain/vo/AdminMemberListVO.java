package com.zhanglx.sso.auth.domain.vo;

import com.zhanglx.sso.auth.enums.MemberTypeEnum;
import com.zhanglx.sso.auth.enums.RealNameStatusEnum;
import com.zhanglx.sso.auth.enums.UserStatusEnum;
import com.zhanglx.sso.core.config.StringToLongDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;

/**
 * 后台会员列表展示对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AdminMemberListVO", description = "后台会员列表展示对象")
public class AdminMemberListVO {

    /**
     * 会员 ID。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long id;

    /**
     * 手机号。
     */
    private String phoneNumber;

    /**
     * 昵称。
     */
    private String nickname;

    /**
     * 头像。
     */
    private String avatar;

    /**
     * 状态。
     */
    private UserStatusEnum status;

    /**
     * 实名状态。
     */
    private RealNameStatusEnum realNameStatus;

    /**
     * 会员类型。
     */
    private MemberTypeEnum memberType;

    /**
     * 用户等级。
     */
    private Integer userLevel;

    /**
     * 积分。
     */
    private Long points;

    /**
     * 是否已绑定手机号。
     */
    private Boolean phoneBound;

    /**
     * 是否已绑定微信。
     */
    private Boolean wechatBound;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 最后登录时间。
     */
    private LocalDateTime lastLoginTime;

    /**
     * 注册 IP。
     */
    private String registerIp;

    /**
     * 最后登录 IP。
     */
    private String lastLoginIp;
}