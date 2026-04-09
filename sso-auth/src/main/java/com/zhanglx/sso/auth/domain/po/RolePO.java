package com.zhanglx.sso.auth.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhanglx.sso.auth.enums.DataScopeEnum;
import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.mybatis.domain.po.BasePO;
import com.zhanglx.sso.mybatis.handler.AutoEnumTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.ibatis.type.JdbcType;

/**
 * 角色持久化对象，对应 V2 表 `t_auth_role`。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_auth_role", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class RolePO extends BasePO {

    @TableField(value = "role_name", jdbcType = JdbcType.VARCHAR)
    private String roleName;

    @TableField(value = "role_code", jdbcType = JdbcType.VARCHAR)
    private String roleCode;

    @TableField(value = "app_code", jdbcType = JdbcType.VARCHAR)
    private String appCode;

    @TableField(value = "data_scope", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private DataScopeEnum dataScope;

    @TableField(value = "status", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private EnableStatusEnum status;

    @TableField(value = "remark", jdbcType = JdbcType.VARCHAR)
    private String remark;
}
