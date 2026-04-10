package com.zhanglx.sso.auth.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.auth.enums.UserTypeEnum;
import com.zhanglx.sso.mybatis.domain.po.BasePO;
import com.zhanglx.sso.mybatis.handler.AutoEnumTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.ibatis.type.JdbcType;

/**
 * 应用持久化对象，对应表 `t_sso_app`。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_sso_app", autoResultMap = true)
public class AppPO extends BasePO {

    /**
     * 应用编码。
     */
    @TableField(value = "app_code", jdbcType = JdbcType.VARCHAR)
    private String appCode;

    /**
     * 应用名称。
     */
    @TableField(value = "app_name", jdbcType = JdbcType.VARCHAR)
    private String appName;

    // 显式绑定枚举 TypeHandler，确保查询结果阶段也能把数据库值映射为枚举。
    @TableField(value = "status", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private EnableStatusEnum status;

    /**
     * 用户类型。
     */
    @TableField(value = "user_type", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private UserTypeEnum userType;

    /**
     * 备注。
     */
    @TableField(value = "remark", jdbcType = JdbcType.VARCHAR)
    private String remark;
}
