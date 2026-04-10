package com.zhanglx.sso.auth.mapper;

import com.zhanglx.sso.auth.domain.po.RolePO;
import com.zhanglx.sso.auth.enums.DataScopeEnum;
import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.mybatis.handler.AutoEnumTypeHandler;
import com.zhanglx.sso.mybatis.mapper.IBaseMapperX;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 作者：Zhang L X
 * 创建时间：2026/3/17 15:04
 * 类名：RoleMapper
 * 说明：角色数据访问层
 */
@Mapper
public interface RoleMapper extends IBaseMapperX<RolePO> {

    /**
     * 根据用户名查询用户已绑定的角色列表。
     *
     * @param userAccount 用户账号
     * @return 当前账号绑定的角色列表
     */
    /**
     * 根据用户账号查询可用角色列表。
     */
    @Results(id = "rolePoResultMap", value = {
            @Result(property = "dataScope", column = "data_scope", javaType = DataScopeEnum.class,
                    typeHandler = AutoEnumTypeHandler.class),
            @Result(property = "status", column = "status", javaType = EnableStatusEnum.class,
                    typeHandler = AutoEnumTypeHandler.class)
    })
    @Select("SELECT r.id, r.app_code, r.role_name, r.role_code, r.data_scope, r.status, r.remark, " +
            "r.create_by, r.create_time, r.update_by, r.update_time " +
            "FROM t_sys_user u " +
            "INNER JOIN t_auth_user_role urm ON u.id = urm.user_id " +
            "INNER JOIN t_auth_role r ON urm.role_id = r.id " +
            "WHERE u.username = #{userAccount} " +
            "  AND r.del_flag = 0 " +
            "  AND urm.del_flag = 0 " +
            "  AND u.del_flag = 0")
    List<RolePO> selectRolesForUser(@Param("userAccount") String userAccount);

}