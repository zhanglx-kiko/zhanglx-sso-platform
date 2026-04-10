package com.zhanglx.sso.auth.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhanglx.sso.auth.enums.GenderEnum;
import com.zhanglx.sso.auth.enums.UserStatusEnum;
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
    @TableField(value = "member_type", jdbcType = JdbcType.TINYINT)
    private Integer memberType;

    /**
     * 实名状态。
     */
    @TableField(value = "real_name_status", jdbcType = JdbcType.TINYINT)
    private Integer realNameStatus;

    /**
     * 扩展资料统一收敛为 序列化文本，避免会员体系频繁改表。
     */
    @TableField(value = "profile_extra", jdbcType = JdbcType.LONGVARCHAR)
    private String profileExtra;

    /**
     * 状态。
     */
    @TableField(value = "status", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private UserStatusEnum status;

    /**
     * 注册IP。
     */
    @TableField(value = "register_ip", jdbcType = JdbcType.VARCHAR)
    private String registerIp;

    /**
     * 最后登录时间。
     */
    @TableField(value = "last_login_time", jdbcType = JdbcType.TIMESTAMP)
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录地址。
     */
    @TableField(value = "last_login_ip", jdbcType = JdbcType.VARCHAR)
    private String lastLoginIp;
}