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

/**
 * 系统配置持久化对象。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_sys_config", autoResultMap = true)
public class ConfigPO extends BasePO {

    /**
     * 配置名称。
     */
    @TableField(value = "config_name", jdbcType = JdbcType.VARCHAR)
    private String configName;

    /**
     * 配置键。
     */
    @TableField(value = "config_key", jdbcType = JdbcType.VARCHAR)
    private String configKey;

    /**
     * 配置值。
     */
    @TableField(value = "config_value", jdbcType = JdbcType.VARCHAR)
    private String configValue;

    /**
     * 配置类型。
     */
    @TableField(value = "config_type", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private ConfigTypeEnum configType;

    /**
     * 备注。
     */
    @TableField(value = "remark", jdbcType = JdbcType.VARCHAR)
    private String remark;
}