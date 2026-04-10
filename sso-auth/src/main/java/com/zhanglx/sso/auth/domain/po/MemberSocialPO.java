package com.zhanglx.sso.auth.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhanglx.sso.auth.enums.SocialIdentityTypeEnum;
import com.zhanglx.sso.mybatis.domain.po.BasePO;
import com.zhanglx.sso.mybatis.handler.AutoEnumTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.ibatis.type.JdbcType;

/**
 * 会员第三方账号持久化对象。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_member_social", autoResultMap = true)
public class MemberSocialPO extends BasePO {

    /**
     * 会员标识。
     */
    @TableField(value = "member_id", jdbcType = JdbcType.BIGINT)
    private Long memberId;

    /**
     * 身份类型。
     */
    @TableField(value = "identity_type", jdbcType = JdbcType.VARCHAR, typeHandler = AutoEnumTypeHandler.class)
    private SocialIdentityTypeEnum identityType;

    /**
     * 唯一标识。
     */
    @TableField(value = "identifier", jdbcType = JdbcType.VARCHAR)
    private String identifier;

    /**
     * Union标识。
     */
    @TableField(value = "union_id", jdbcType = JdbcType.VARCHAR)
    private String unionId;
}