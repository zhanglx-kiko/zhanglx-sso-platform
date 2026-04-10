package com.zhanglx.sso.auth.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
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
 * 部门持久化对象。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_auth_dept", autoResultMap = true)
public class DeptPO extends BasePO {

    /**
     * 父级ID。
     */
    @TableField(value = "parent_id", jdbcType = JdbcType.BIGINT)
    private Long parentId;

    /**
     * 祖级路径。
     */
    @TableField(value = "ancestors", jdbcType = JdbcType.VARCHAR)
    private String ancestors;

    /**
     * 部门名称。
     */
    @TableField(value = "dept_name", jdbcType = JdbcType.VARCHAR)
    private String deptName;

    /**
     * 排序号。
     */
    @TableField(value = "sort_num", jdbcType = JdbcType.INTEGER)
    private Integer sortNum;

    /**
     * 状态。
     */
    @TableField(value = "status", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private EnableStatusEnum status;
}
