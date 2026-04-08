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
@TableName(value = "t_sys_dict_type", autoResultMap = true)
public class DictTypePO extends BasePO {

    @TableField(value = "dict_name", jdbcType = JdbcType.VARCHAR)
    private String dictName;

    @TableField(value = "dict_type", jdbcType = JdbcType.VARCHAR)
    private String dictType;

    @TableField(value = "status", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private EnableStatusEnum status;

    @TableField(value = "remark", jdbcType = JdbcType.VARCHAR)
    private String remark;
}
