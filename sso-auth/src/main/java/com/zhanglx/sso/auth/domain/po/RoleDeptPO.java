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
 * 角色部门关系持久化对象。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_auth_role_dept", autoResultMap = true)
public class RoleDeptPO extends BasePO {

    /**
     * 角色ID。
     */
    @TableField(value = "role_id", jdbcType = JdbcType.BIGINT)
    private Long roleId;

    /**
     * 部门ID。
     */
    @TableField(value = "dept_id", jdbcType = JdbcType.BIGINT)
    private Long deptId;
}
