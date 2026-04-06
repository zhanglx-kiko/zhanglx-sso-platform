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

import java.time.LocalDateTime;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/18 10:48
 * @ClassName: RolePermissionRelationshipMappingPO
 * @Description:
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_auth_role_permission", autoResultMap = true)
public class RolePermissionRelationshipMappingPO extends BasePO {

    /**
     * 角色Id
     */
    @TableField(value = "role_id", jdbcType = JdbcType.BIGINT)
    private Long roleId;

    /**
     * 权限项id
     */
    @TableField(value = "permission_id", jdbcType = JdbcType.BIGINT)
    private Long permissionId;

    /**
     * 授权过期时间
     */
    @TableField(exist = false)
    private LocalDateTime expireTime;


}
