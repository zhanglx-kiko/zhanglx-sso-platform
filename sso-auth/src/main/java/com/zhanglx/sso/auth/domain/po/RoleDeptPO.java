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

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_auth_role_dept", autoResultMap = true)
public class RoleDeptPO extends BasePO {

    @TableField(value = "role_id", jdbcType = JdbcType.BIGINT)
    private Long roleId;

    @TableField(value = "dept_id", jdbcType = JdbcType.BIGINT)
    private Long deptId;
}
