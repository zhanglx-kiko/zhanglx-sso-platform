package com.zhanglx.sso.auth.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhanglx.sso.auth.enums.ConfigTypeEnum;
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
@TableName(value = "t_sys_config", autoResultMap = true)
public class ConfigPO extends BasePO {

    @TableField(value = "config_name", jdbcType = JdbcType.VARCHAR)
    private String configName;

    @TableField(value = "config_key", jdbcType = JdbcType.VARCHAR)
    private String configKey;

    @TableField(value = "config_value", jdbcType = JdbcType.VARCHAR)
    private String configValue;

    @TableField(value = "config_type", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private ConfigTypeEnum configType;

    @TableField(value = "remark", jdbcType = JdbcType.VARCHAR)
    private String remark;
}
