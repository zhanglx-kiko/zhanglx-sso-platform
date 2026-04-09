package com.zhanglx.sso.auth.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhanglx.sso.auth.enums.GenderEnum;
import com.zhanglx.sso.auth.enums.UserStatusEnum;
import com.zhanglx.sso.auth.enums.UserTypeEnum;
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

/**
 * 系统用户持久化对象，对应 V2 表 `t_sys_user`。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_sys_user", autoResultMap = true)
public class UserPO extends BasePO {

    @TableField(value = "username", jdbcType = JdbcType.VARCHAR)
    private String username;

    @TableField(value = "password", jdbcType = JdbcType.VARCHAR)
    private String password;

    @TableField(value = "user_type", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private UserTypeEnum userType;

    /**
     * 后台用户的微信 openId 独立维护在 `t_sys_user_social` 中，这里仅作为业务装配字段使用。
     */
    @TableField(exist = false)
    private String openId;

    @TableField(value = "nickname", jdbcType = JdbcType.VARCHAR)
    private String nickname;

    @TableField(value = "avatar", jdbcType = JdbcType.VARCHAR)
    private String avatar;

    @TableField(value = "phone_number", jdbcType = JdbcType.VARCHAR)
    private String phoneNumber;

    @TableField(value = "sex", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private GenderEnum sex;

    @TableField(value = "birthday", jdbcType = JdbcType.DATE)
    private LocalDate birthday;

    @TableField(value = "email", jdbcType = JdbcType.VARCHAR)
    private String email;

    @TableField(value = "allow_concurrent_login", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private YesNoEnum allowConcurrentLogin;

    @TableField(value = "dept_id", jdbcType = JdbcType.BIGINT)
    private Long deptId;

    /**
     * 状态：1-正常，0-禁用
     */
    @TableField(value = "status", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private UserStatusEnum status;
}
