package com.zhanglx.sso.auth.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhanglx.sso.auth.enums.GenderEnum;
import com.zhanglx.sso.auth.enums.MemberTypeEnum;
import com.zhanglx.sso.auth.enums.RealNameStatusEnum;
import com.zhanglx.sso.auth.enums.UserStatusEnum;
import com.zhanglx.sso.auth.enums.YesNoEnum;
import com.zhanglx.sso.mybatis.domain.po.BasePO;
import com.zhanglx.sso.mybatis.handler.AutoEnumTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * C 端会员持久化对象。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_member_user", autoResultMap = true)
public class MemberUserPO extends BasePO {

    /**
     * 手机号。
     */
    @TableField(value = "phone_number", jdbcType = JdbcType.VARCHAR)
    private String phoneNumber;

    /**
     * 密码。
     */
    @TableField(value = "password", jdbcType = JdbcType.VARCHAR)
    private String password;

    /**
     * 昵称。
     */
    @TableField(value = "nickname", jdbcType = JdbcType.VARCHAR)
    private String nickname;

    /**
     * 头像。
     */
    @TableField(value = "avatar", jdbcType = JdbcType.VARCHAR)
    private String avatar;

    /**
     * 性别。
     */
    @TableField(value = "sex", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private GenderEnum sex;

    /**
     * 生日。
     */
    @TableField(value = "birthday", jdbcType = JdbcType.DATE)
    private LocalDate birthday;

    /**
     * 邮箱。
     */
    @TableField(value = "email", jdbcType = JdbcType.VARCHAR)
    private String email;

    /**
     * 是否已绑定手机号。
     */
    @TableField(value = "phone_bound", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private YesNoEnum phoneBound;

    /**
     * 用户等级。
     */
    @TableField(value = "user_level", jdbcType = JdbcType.INTEGER)
    private Integer userLevel;

    /**
     * 积分。
     */
    @TableField(value = "points", jdbcType = JdbcType.BIGINT)
    private Long points;

    /**
     * 会员类型。
     */
    @TableField(value = "member_type", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private MemberTypeEnum memberType;

    /**
     * 实名状态。
     */
    @TableField(value = "real_name_status", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private RealNameStatusEnum realNameStatus;

    /**
     * 注册来源。
     */
    @TableField(value = "register_source", jdbcType = JdbcType.VARCHAR)
    private String registerSource;

    /**
     * 注册设备。
     */
    @TableField(value = "register_device", jdbcType = JdbcType.VARCHAR)
    private String registerDevice;

    /**
     * 风险等级。
     */
    @TableField(value = "risk_level", jdbcType = JdbcType.INTEGER)
    private Integer riskLevel;

    /**
     * 是否黑名单。
     */
    @TableField(value = "blacklist_flag", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private YesNoEnum blacklistFlag;

    /**
     * 扩展资料，统一收敛成 JSON 文本，避免频繁改表。
     */
    @TableField(value = "profile_extra", jdbcType = JdbcType.LONGVARCHAR)
    private String profileExtra;

    /**
     * 会员状态。
     */
    @TableField(value = "status", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private UserStatusEnum status;

    /**
     * 状态原因。
     */
    @TableField(value = "status_reason", jdbcType = JdbcType.VARCHAR)
    private String statusReason;

    /**
     * 状态到期时间。
     */
    @TableField(value = "status_expire_time", jdbcType = JdbcType.TIMESTAMP)
    private LocalDateTime statusExpireTime;

    /**
     * 注销时间。
     */
    @TableField(value = "cancel_time", jdbcType = JdbcType.TIMESTAMP)
    private LocalDateTime cancelTime;

    /**
     * 禁用时间。
     */
    @TableField(value = "disabled_time", jdbcType = JdbcType.TIMESTAMP)
    private LocalDateTime disabledTime;

    /**
     * 注册 IP。
     */
    @TableField(value = "register_ip", jdbcType = JdbcType.VARCHAR)
    private String registerIp;

    /**
     * 最后登录时间。
     */
    @TableField(value = "last_login_time", jdbcType = JdbcType.TIMESTAMP)
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录 IP。
     */
    @TableField(value = "last_login_ip", jdbcType = JdbcType.VARCHAR)
    private String lastLoginIp;
}