package com.zhanglx.sso.auth.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.auth.enums.PermissionTypeEnum;
import com.zhanglx.sso.auth.enums.YesNoEnum;
import com.zhanglx.sso.mybatis.domain.po.BasePO;
import com.zhanglx.sso.mybatis.handler.AutoEnumTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.ibatis.type.JdbcType;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/17 15:12
 * @ClassName: PermissionPO
 * @Description: 权限对象
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_auth_permission", autoResultMap = true)
public class PermissionPO extends BasePO {

    /**
     * 权限项名称
     */
    @TableField(value = "name", jdbcType = JdbcType.VARCHAR)
    private String name;

    /**
     * 权限项标识
     */
    @TableField(value = "identification", jdbcType = JdbcType.VARCHAR)
    private String identification;

    /**
     * 父ID
     */
    @TableField(value = "parent_id", jdbcType = JdbcType.BIGINT)
    private Long parentId;

    /**
     * 标识血缘
     */
    @TableField(value = "identity_lineage", jdbcType = JdbcType.VARCHAR)
    private String identityLineage;

    /**
     * 组件地址
     */
    @TableField(value = "com_path", jdbcType = JdbcType.VARCHAR)
    private String comPath;

    /**
     * 路由地址
     */
    @TableField(value = "path", jdbcType = JdbcType.VARCHAR)
    private String path;

    /**
     * 关联图标
     */
    @TableField(value = "icon_str", jdbcType = JdbcType.VARCHAR)
    private String iconStr;

    /**
     * 显示顺序
     */
    @TableField(value = "display_no", jdbcType = JdbcType.INTEGER)
    private Integer displayNo;

    /**
     * 是否为外链(1是 0否)
     */
    @TableField(value = "is_frame", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private YesNoEnum isFrame;

    /**
     * 类型(-1平台 0模块 1菜单 2按钮 3接口)
     */
    @TableField(value = "type", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private PermissionTypeEnum type;

    /**
     * 备注
     */
    @TableField(value = "remark", jdbcType = JdbcType.VARCHAR)
    private String remark;

    /**
     * 状态。
     */
    @TableField(value = "status", jdbcType = JdbcType.TINYINT, typeHandler = AutoEnumTypeHandler.class)
    private EnableStatusEnum status;

}
