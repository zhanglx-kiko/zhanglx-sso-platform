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

    /**
     * 用户名。
     */
    @TableField(value = "username", jdbcType = JdbcType.VARCHAR)
    private String username;

    /**
     * 密码。
     */
    @TableField(value = "password", jdbcType = JdbcType.VARCHAR)
    private String password;

    /**
     * 用户类型。
     */
    @TableField(value = "user_type", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private UserTypeEnum userType;

    /**
     * 后台用户的微信 openId 独立维护在 `t_sys_user_social` 中，这里仅作为业务装配字段使用。
     */
    @TableField(exist = false)
    private String openId;

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
     * 手机号。
     */
    @TableField(value = "phone_number", jdbcType = JdbcType.VARCHAR)
    private String phoneNumber;

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
     * 是否允许并发登录。
     */
    @TableField(value = "allow_concurrent_login", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private YesNoEnum allowConcurrentLogin;

    /**
     * 部门ID。
     */
    @TableField(value = "dept_id", jdbcType = JdbcType.BIGINT)
    private Long deptId;

    /**
     * 状态：1-正常，0-禁用
     */
    @TableField(value = "status", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private UserStatusEnum status;
}
