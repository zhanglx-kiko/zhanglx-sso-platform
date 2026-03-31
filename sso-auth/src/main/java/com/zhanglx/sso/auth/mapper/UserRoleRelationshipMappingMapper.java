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
 * @Description:
 */
@Mapper
public interface UserRoleRelationshipMappingMapper extends IBaseMapperX<UserRoleRelationshipMappingPO> {

    /**
     * 根据角色 ID 查询关联的用户 ID 列表
     */
    @Select("SELECT user_id FROM t_auth_user_role_mapping WHERE role_id = #{roleId} AND del_flag = 0")
    List<Long> selUserIdListByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户 ID 查询关联的角色 ID 列表
     */
    @Select("SELECT role_id FROM t_auth_user_role_mapping WHERE user_id = #{userId} AND del_flag = 0")
    List<Long> selRoleIdsByUserId(@Param("userId") Long userId);

}
