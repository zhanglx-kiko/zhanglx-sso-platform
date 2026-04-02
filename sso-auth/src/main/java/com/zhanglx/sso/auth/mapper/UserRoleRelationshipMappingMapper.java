package com.zhanglx.sso.auth.mapper;

import com.zhanglx.sso.auth.domain.po.UserRoleRelationshipMappingPO;
import com.zhanglx.sso.mybatis.mapper.IBaseMapperX;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/20 10:26
 * @ClassName: UserRoleRelationshipMappingMapper
 * @Description: 用户角色关联数据访问层
 */
@Mapper
public interface UserRoleRelationshipMappingMapper extends IBaseMapperX<UserRoleRelationshipMappingPO> {

    /**
     * 根据角色 ID 查询已绑定的用户 ID 列表。
     *
     * @param roleId 角色 ID
     * @return 绑定到该角色的用户 ID 列表
     */
    @Select("SELECT user_id FROM t_auth_user_role_mapping WHERE role_id = #{roleId} AND del_flag = 0")
    List<Long> selUserIdListByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户 ID 查询已绑定的角色 ID 列表。
     *
     * @param userId 用户 ID
     * @return 该用户绑定的角色 ID 列表
     */
    @Select("SELECT role_id FROM t_auth_user_role_mapping WHERE user_id = #{userId} AND del_flag = 0")
    List<Long> selRoleIdsByUserId(@Param("userId") Long userId);

}