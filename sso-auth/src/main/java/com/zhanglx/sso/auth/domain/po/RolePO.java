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
 * @Create: 2026/3/18 11:34
 * @ClassName: RolePO
 * @Description:
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_auth_role", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class RolePO extends BasePO {

    /**
     * 角色名称
     */
    @TableField(value = "role_name", jdbcType = JdbcType.VARCHAR)
    private String roleName;

    /**
     * 角色编码
     */
    @TableField(value = "role_code", jdbcType = JdbcType.VARCHAR)
    private String roleCode;

    /**
     * 角色类型
     */
    @TableField(value = "role_type", jdbcType = JdbcType.VARCHAR)
    private String roleType;

    /**
     * 是否内置:1-内置,0-非内置
     */
    @TableField(value = "build_in", jdbcType = JdbcType.TINYINT)
    private Integer buildIn;

    /**
     * 备注
     */
    @TableField(value = "remark", jdbcType = JdbcType.VARCHAR)
    private String remark;

}
