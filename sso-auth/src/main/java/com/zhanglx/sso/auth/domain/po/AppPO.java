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
@TableName(value = "t_sso_app", autoResultMap = true)
public class AppPO extends BasePO {

    @TableField(value = "app_code", jdbcType = JdbcType.VARCHAR)
    private String appCode;

    @TableField(value = "app_name", jdbcType = JdbcType.VARCHAR)
    private String appName;

    @TableField(value = "status", jdbcType = JdbcType.TINYINT)
    private Integer status;

    @TableField(value = "user_type", jdbcType = JdbcType.TINYINT)
    private Integer userType;

    @TableField(value = "remark", jdbcType = JdbcType.VARCHAR)
    private String remark;
}
