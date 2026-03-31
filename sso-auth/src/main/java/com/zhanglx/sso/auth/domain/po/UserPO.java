package com.zhanglx.sso.auth.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhanglx.sso.mybatis.domain.po.BasePO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.ibatis.type.JdbcType;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/10 20:45
 * @ClassName: UserPO
 * @Description: 用户持久化对象 (对应数据库表)
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_auth_user", autoResultMap = true)
public class UserPO extends BasePO {

    @TableField(value = "username", jdbcType = JdbcType.VARCHAR)
    private String username;

    @TableField(value = "password", jdbcType = JdbcType.VARCHAR)
    private String password;

    @TableField(value = "nickname", jdbcType = JdbcType.VARCHAR)
    private String nickname;

    @TableField(value = "avatar", jdbcType = JdbcType.VARCHAR)
    private String avatar;

    @TableField(value = "openId", jdbcType = JdbcType.VARCHAR)
    private String openId;

    /**
     * 是否允许并发登录：0-禁止(会顶号)，1-允许(默认)
     */
    @TableField(value = "allow_concurrent_login", jdbcType = JdbcType.INTEGER)
    private Integer allowConcurrentLogin;

    /**
     * 部门ID
     */
    @TableField(value = "dept_id", jdbcType = JdbcType.BIGINT)
    private Long deptId;

    @TableField(value = "status", jdbcType = JdbcType.INTEGER)
    private Integer status;

}
