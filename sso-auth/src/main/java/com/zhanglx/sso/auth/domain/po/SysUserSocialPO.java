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
 * 后台用户第三方账号持久化对象。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_sys_user_social", autoResultMap = true)
public class SysUserSocialPO extends BasePO {

    /**
     * 用户ID。
     */
    @TableField(value = "user_id", jdbcType = JdbcType.BIGINT)
    private Long userId;

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
     * 凭证。
     */
    @TableField(value = "credential", jdbcType = JdbcType.VARCHAR)
    private String credential;
}
