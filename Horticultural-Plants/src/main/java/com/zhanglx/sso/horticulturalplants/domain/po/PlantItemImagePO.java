package com.zhanglx.sso.horticulturalplants.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhanglx.sso.horticulturalplants.enums.YesNoEnum;
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
@TableName(value = "t_horticultural_plant_item_image", autoResultMap = true)
public class PlantItemImagePO extends BasePO {

    @TableField(value = "plant_item_id", jdbcType = JdbcType.BIGINT)
    private Long plantItemId;

    @TableField(value = "image_url", jdbcType = JdbcType.VARCHAR)
    private String imageUrl;

    @TableField(value = "sort_num", jdbcType = JdbcType.INTEGER)
    private Integer sortNum;

    @TableField(value = "cover_flag", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private YesNoEnum coverFlag;
}
