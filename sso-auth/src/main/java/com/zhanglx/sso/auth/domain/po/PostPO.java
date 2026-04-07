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
@TableName(value = "t_auth_post", autoResultMap = true)
public class PostPO extends BasePO {

    @TableField(value = "post_code", jdbcType = JdbcType.VARCHAR)
    private String postCode;

    @TableField(value = "post_name", jdbcType = JdbcType.VARCHAR)
    private String postName;

    @TableField(value = "sort_num", jdbcType = JdbcType.INTEGER)
    private Integer sortNum;

    @TableField(value = "status", jdbcType = JdbcType.TINYINT)
    private Integer status;
}
