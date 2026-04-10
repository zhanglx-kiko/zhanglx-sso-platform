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

    /**
     * 角色名称。
     */
    @TableField(value = "role_name", jdbcType = JdbcType.VARCHAR)
    private String roleName;

    /**
     * 角色编码。
     */
    @TableField(value = "role_code", jdbcType = JdbcType.VARCHAR)
    private String roleCode;

    /**
     * 应用编码。
     */
    @TableField(value = "app_code", jdbcType = JdbcType.VARCHAR)
    private String appCode;

    /**
     * 数据范围。
     */
    @TableField(value = "data_scope", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private DataScopeEnum dataScope;

    /**
     * 状态。
     */
    @TableField(value = "status", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private EnableStatusEnum status;

    /**
     * 备注。
     */
    @TableField(value = "remark", jdbcType = JdbcType.VARCHAR)
    private String remark;
}