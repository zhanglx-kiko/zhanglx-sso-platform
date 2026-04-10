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
 * @Create: 2026/3/20 10:27
 * @ClassName: UserRoleRelationshipMappingPO
 * @Description: 用户与角色关系映射表
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_auth_user_role", autoResultMap = true)
public class UserRoleRelationshipMappingPO extends BasePO {

    /**
     * 用户id
     */
    @TableField(value = "user_id", jdbcType = JdbcType.BIGINT)
    private Long userId;

    /**
     * 角色Id
     */
    @TableField(value = "role_id", jdbcType = JdbcType.BIGINT)
    private Long roleId;

    /**
     * 用户信息。
     */
    @TableField(exist = false)
    private UserPO user;

    /**
     * 角色信息。
     */
    @TableField(exist = false)
    private RolePO role;

}
