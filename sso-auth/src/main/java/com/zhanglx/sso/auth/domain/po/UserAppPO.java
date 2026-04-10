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

/**
 * 用户应用关系持久化对象。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_auth_user_app", autoResultMap = true)
public class UserAppPO extends BasePO {

    /**
     * 用户标识。
     */
    @TableField(value = "user_id", jdbcType = JdbcType.BIGINT)
    private Long userId;

    /**
     * 应用编码。
     */
    @TableField(value = "app_code", jdbcType = JdbcType.VARCHAR)
    private String appCode;
}