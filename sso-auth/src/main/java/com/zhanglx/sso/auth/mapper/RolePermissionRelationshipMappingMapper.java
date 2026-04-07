package com.zhanglx.sso.auth.mapper;

import com.zhanglx.sso.auth.domain.po.RolePermissionRelationshipMappingPO;
import com.zhanglx.sso.mybatis.mapper.IBaseMapperX;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/18 11:37
 * @ClassName: RolePermissionRelationshipMappingMapper
 * @Description:
 */
@Mapper
public interface RolePermissionRelationshipMappingMapper extends IBaseMapperX<RolePermissionRelationshipMappingPO> {

    @Update("UPDATE t_auth_role_permission SET del_flag = id WHERE role_id = #{roleId} AND del_flag = 0")
    int deleteByRoleId(@Param("roleId") Long roleId);

    @Update("<script>" +
            "UPDATE t_auth_role_permission SET del_flag = id WHERE role_id = #{roleId} AND del_flag = 0 AND permission_id IN " +
            "<foreach item='permissionId' collection='permissionIds' open='(' separator=',' close=')'>" +
            "#{permissionId}" +
            "</foreach>" +
            "</script>")
    int deleteByRoleIdAndPermissionIds(@Param("roleId") Long roleId,
                                       @Param("permissionIds") List<Long> permissionIds);

    @Update("<script>" +
            "UPDATE t_auth_role_permission SET del_flag = id WHERE role_id IN " +
            "<foreach item='roleId' collection='roleIds' open='(' separator=',' close=')'>" +
            "#{roleId}" +
            "</foreach> AND del_flag = 0" +
            "</script>")
    int deleteByRoleIds(@Param("roleIds") List<Long> roleIds);

    @Update("<script>" +
            "UPDATE t_auth_role_permission SET del_flag = id WHERE permission_id IN " +
            "<foreach item='permissionId' collection='permissionIds' open='(' separator=',' close=')'>" +
            "#{permissionId}" +
            "</foreach> AND del_flag = 0" +
            "</script>")
    int deleteByPermissionIds(@Param("permissionIds") List<Long> permissionIds);
}
