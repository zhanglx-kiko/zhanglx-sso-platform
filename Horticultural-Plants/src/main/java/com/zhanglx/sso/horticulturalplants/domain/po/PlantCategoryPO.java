package com.zhanglx.sso.horticulturalplants.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhanglx.sso.horticulturalplants.enums.EnableStatusEnum;
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
@TableName(value = "t_horticultural_plant_category", autoResultMap = true)
public class PlantCategoryPO extends BasePO {

    @TableField(value = "category_name", jdbcType = JdbcType.VARCHAR)
    private String categoryName;

    @TableField(value = "category_code", jdbcType = JdbcType.VARCHAR)
    private String categoryCode;

    @TableField(value = "sort_num", jdbcType = JdbcType.INTEGER)
    private Integer sortNum;

    @TableField(value = "status", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private EnableStatusEnum status;

    @TableField(value = "description", jdbcType = JdbcType.VARCHAR)
    private String description;
}
