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
 * 字典数据持久化对象。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_sys_dict_data", autoResultMap = true)
public class DictDataPO extends BasePO {

    /**
     * 字典排序号。
     */
    @TableField(value = "dict_sort", jdbcType = JdbcType.INTEGER)
    private Integer dictSort;

    /**
     * 字典标签。
     */
    @TableField(value = "dict_label", jdbcType = JdbcType.VARCHAR)
    private String dictLabel;

    /**
     * 字典值。
     */
    @TableField(value = "dict_value", jdbcType = JdbcType.VARCHAR)
    private String dictValue;

    /**
     * 字典类型。
     */
    @TableField(value = "dict_type", jdbcType = JdbcType.VARCHAR)
    private String dictType;

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
