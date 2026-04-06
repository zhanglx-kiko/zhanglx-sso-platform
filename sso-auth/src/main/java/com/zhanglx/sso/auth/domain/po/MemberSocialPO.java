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
@TableName(value = "t_member_social", autoResultMap = true)
public class MemberSocialPO extends BasePO {

    @TableField(value = "member_id", jdbcType = JdbcType.BIGINT)
    private Long memberId;

    @TableField(value = "identity_type", jdbcType = JdbcType.VARCHAR)
    private String identityType;

    @TableField(value = "identifier", jdbcType = JdbcType.VARCHAR)
    private String identifier;

    @TableField(value = "union_id", jdbcType = JdbcType.VARCHAR)
    private String unionId;
}
