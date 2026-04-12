package com.zhanglx.sso.horticulturalplants.domain.po;

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
@TableName(value = "t_member_user", autoResultMap = true)
public class PlantMemberUserPO extends BasePO {

    @TableField(value = "phone_number", jdbcType = JdbcType.VARCHAR)
    private String phoneNumber;

    @TableField(value = "nickname", jdbcType = JdbcType.VARCHAR)
    private String nickname;

    @TableField(value = "avatar", jdbcType = JdbcType.VARCHAR)
    private String avatar;

    @TableField(value = "status", jdbcType = JdbcType.TINYINT)
    private Integer status;
}
