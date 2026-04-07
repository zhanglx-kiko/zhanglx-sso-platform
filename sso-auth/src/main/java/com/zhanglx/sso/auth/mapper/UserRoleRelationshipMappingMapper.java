package com.zhanglx.sso.auth.mapper;

import com.zhanglx.sso.auth.domain.po.UserRoleRelationshipMappingPO;
import com.zhanglx.sso.mybatis.mapper.IBaseMapperX;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
    @Select("SELECT user_id FROM t_auth_user_role WHERE role_id = #{roleId} AND del_flag = 0")
    List<Long> selUserIdListByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户 ID 查询已绑定的角色 ID 列表。
     *
     * @param userId 用户 ID
     * @return 该用户绑定的角色 ID 列表
     */
    @Select("SELECT role_id FROM t_auth_user_role WHERE user_id = #{userId} AND del_flag = 0")
    List<Long> selRoleIdsByUserId(@Param("userId") Long userId);

    @Update("UPDATE t_auth_user_role SET del_flag = id WHERE role_id = #{roleId} AND del_flag = 0")
    int deleteByRoleId(@Param("roleId") Long roleId);

    @Update("<script>" +
            "UPDATE t_auth_user_role SET del_flag = id WHERE role_id = #{roleId} AND del_flag = 0 AND user_id IN " +
            "<foreach item='userId' collection='userIds' open='(' separator=',' close=')'>" +
            "#{userId}" +
            "</foreach>" +
            "</script>")
    int deleteByRoleIdAndUserIds(@Param("roleId") Long roleId, @Param("userIds") List<Long> userIds);

    @Update("<script>" +
            "UPDATE t_auth_user_role SET del_flag = id WHERE role_id IN " +
            "<foreach item='roleId' collection='roleIds' open='(' separator=',' close=')'>" +
            "#{roleId}" +
            "</foreach> AND del_flag = 0" +
            "</script>")
    int deleteByRoleIds(@Param("roleIds") List<Long> roleIds);

    @Update("UPDATE t_auth_user_role SET del_flag = id WHERE user_id = #{userId} AND del_flag = 0")
    int deleteByUserId(@Param("userId") Long userId);

    @Update("<script>" +
            "UPDATE t_auth_user_role SET del_flag = id WHERE user_id IN " +
            "<foreach item='userId' collection='userIds' open='(' separator=',' close=')'>" +
            "#{userId}" +
            "</foreach> AND del_flag = 0" +
            "</script>")
    int deleteByUserIds(@Param("userIds") List<Long> userIds);

}
