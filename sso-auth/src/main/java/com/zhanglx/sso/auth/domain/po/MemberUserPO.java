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

    @TableField(value = "phone_number", jdbcType = JdbcType.VARCHAR)
    private String phoneNumber;

    @TableField(value = "password", jdbcType = JdbcType.VARCHAR)
    private String password;

    @TableField(value = "nickname", jdbcType = JdbcType.VARCHAR)
    private String nickname;

    @TableField(value = "avatar", jdbcType = JdbcType.VARCHAR)
    private String avatar;

    @TableField(value = "sex", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private GenderEnum sex;

    @TableField(value = "birthday", jdbcType = JdbcType.DATE)
    private LocalDate birthday;

    @TableField(value = "email", jdbcType = JdbcType.VARCHAR)
    private String email;

    @TableField(value = "user_level", jdbcType = JdbcType.INTEGER)
    private Integer userLevel;

    @TableField(value = "points", jdbcType = JdbcType.BIGINT)
    private Long points;

    @TableField(value = "member_type", jdbcType = JdbcType.TINYINT)
    private Integer memberType;

    @TableField(value = "real_name_status", jdbcType = JdbcType.TINYINT)
    private Integer realNameStatus;

    /**
     * 扩展资料统一收敛为 JSON 字符串，避免会员体系频繁改表。
     */
    @TableField(value = "profile_extra", jdbcType = JdbcType.LONGVARCHAR)
    private String profileExtra;

    @TableField(value = "status", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private UserStatusEnum status;

    @TableField(value = "register_ip", jdbcType = JdbcType.VARCHAR)
    private String registerIp;

    @TableField(value = "last_login_time", jdbcType = JdbcType.TIMESTAMP)
    private LocalDateTime lastLoginTime;

    @TableField(value = "last_login_ip", jdbcType = JdbcType.VARCHAR)
    private String lastLoginIp;
}
