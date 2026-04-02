package com.zhanglx.sso.auth.mapper;

import com.zhanglx.sso.auth.domain.po.RolePO;
import com.zhanglx.sso.mybatis.mapper.IBaseMapperX;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/17 15:04
 * @ClassName: RoleMapper
 * @Description: 角色数据访问层
 */
@Mapper
public interface RoleMapper extends IBaseMapperX<RolePO> {

    /**
     * 根据用户名查询用户已绑定的角色列表。
     *
     * @param userAccount 用户账号
     * @return 当前账号绑定的角色列表
     */
    @Select("SELECT r.id, r.role_name, r.role_code, r.role_type, r.build_in, r.remark " +
            "FROM t_auth_user u " +
            "INNER JOIN t_auth_user_role_mapping urm ON u.id = urm.user_id " +
            "INNER JOIN t_auth_role r ON urm.role_id = r.id " +
            "WHERE u.username = #{userAccount} " +
            "  AND r.del_flag = 0 " +
            "  AND urm.del_flag = 0 " +
            "  AND u.del_flag = 0")
    List<RolePO> selectRolesForUser(@Param("userAccount") String userAccount);

}