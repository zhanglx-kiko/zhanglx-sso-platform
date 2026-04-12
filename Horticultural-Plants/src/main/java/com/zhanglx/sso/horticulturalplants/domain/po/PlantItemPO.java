package com.zhanglx.sso.horticulturalplants.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhanglx.sso.horticulturalplants.enums.PlantItemPublishStatusEnum;
import com.zhanglx.sso.mybatis.domain.po.BasePO;
import com.zhanglx.sso.mybatis.handler.AutoEnumTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.ibatis.type.JdbcType;

import java.math.BigDecimal;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_horticultural_plant_item", autoResultMap = true)
public class PlantItemPO extends BasePO {

    @TableField(value = "publisher_user_id", jdbcType = JdbcType.BIGINT)
    private Long publisherUserId;

    @TableField(value = "category_id", jdbcType = JdbcType.BIGINT)
    private Long categoryId;

    @TableField(value = "category_name", jdbcType = JdbcType.VARCHAR)
    private String categoryName;

    @TableField(value = "title", jdbcType = JdbcType.VARCHAR)
    private String title;

    @TableField(value = "cover_image_url", jdbcType = JdbcType.VARCHAR)
    private String coverImageUrl;

    @TableField(value = "suggested_retail_price", jdbcType = JdbcType.DECIMAL)
    private BigDecimal suggestedRetailPrice;

    @TableField(value = "unit", jdbcType = JdbcType.VARCHAR)
    private String unit;

    @TableField(value = "short_description", jdbcType = JdbcType.VARCHAR)
    private String shortDescription;

    @TableField(value = "detail_description", jdbcType = JdbcType.LONGVARCHAR)
    private String detailDescription;

    @TableField(value = "province", jdbcType = JdbcType.VARCHAR)
    private String province;

    @TableField(value = "city", jdbcType = JdbcType.VARCHAR)
    private String city;

    @TableField(value = "area", jdbcType = JdbcType.VARCHAR)
    private String area;

    @TableField(value = "publish_status", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private PlantItemPublishStatusEnum publishStatus;

    @TableField(value = "view_count", jdbcType = JdbcType.BIGINT)
    private Long viewCount;
}
