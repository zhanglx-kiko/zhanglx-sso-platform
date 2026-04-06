package com.zhanglx.sso.auth.mapper;

import com.zhanglx.sso.auth.domain.po.RolePermissionRelationshipMappingPO;
import com.zhanglx.sso.mybatis.mapper.IBaseMapperX;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/18 11:37
 * @ClassName: RolePermissionRelationshipMappingMapper
 * @Description:
 */
@Mapper
public interface RolePermissionRelationshipMappingMapper extends IBaseMapperX<RolePermissionRelationshipMappingPO> {

    @Delete("DELETE FROM t_auth_role_permission WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);

    @Delete("<script>" +
            "DELETE FROM t_auth_role_permission WHERE role_id = #{roleId} AND permission_id IN " +
            "<foreach item='permissionId' collection='permissionIds' open='(' separator=',' close=')'>" +
            "#{permissionId}" +
            "</foreach>" +
            "</script>")
    int deleteByRoleIdAndPermissionIds(@Param("roleId") Long roleId,
                                       @Param("permissionIds") List<Long> permissionIds);

    @Delete("<script>" +
            "DELETE FROM t_auth_role_permission WHERE role_id IN " +
            "<foreach item='roleId' collection='roleIds' open='(' separator=',' close=')'>" +
            "#{roleId}" +
            "</foreach>" +
            "</script>")
    int deleteByRoleIds(@Param("roleIds") List<Long> roleIds);

    @Delete("<script>" +
            "DELETE FROM t_auth_role_permission WHERE permission_id IN " +
            "<foreach item='permissionId' collection='permissionIds' open='(' separator=',' close=')'>" +
            "#{permissionId}" +
            "</foreach>" +
            "</script>")
    int deleteByPermissionIds(@Param("permissionIds") List<Long> permissionIds);
}
