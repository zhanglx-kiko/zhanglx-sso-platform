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

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_auth_dept", autoResultMap = true)
public class DeptPO extends BasePO {

    @TableField(value = "parent_id", jdbcType = JdbcType.BIGINT)
    private Long parentId;

    @TableField(value = "ancestors", jdbcType = JdbcType.VARCHAR)
    private String ancestors;

    @TableField(value = "dept_name", jdbcType = JdbcType.VARCHAR)
    private String deptName;

    @TableField(value = "sort_num", jdbcType = JdbcType.INTEGER)
    private Integer sortNum;

    @TableField(value = "status", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private EnableStatusEnum status;
}
