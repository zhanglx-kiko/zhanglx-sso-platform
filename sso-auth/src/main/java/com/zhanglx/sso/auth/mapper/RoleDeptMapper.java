package com.zhanglx.sso.auth.mapper;

import com.zhanglx.sso.auth.domain.po.RoleDeptPO;
import com.zhanglx.sso.mybatis.mapper.IBaseMapperX;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * RoleDept数据访问层。
 */
@Mapper
public interface RoleDeptMapper extends IBaseMapperX<RoleDeptPO> {

    /**
     * 根据角色ID查询已绑定部门ID列表。
     */
    @Select("SELECT dept_id FROM t_auth_role_dept WHERE role_id = #{roleId} AND del_flag = 0")
    List<Long> selectDeptIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据部门ID统计角色部门关联数量。
     */
    @Select("SELECT COUNT(1) FROM t_auth_role_dept WHERE dept_id = #{deptId} AND del_flag = 0")
    Long countByDeptId(@Param("deptId") Long deptId);

    /**
     * 查询第一个引用部门数据权限的角色名称。
     */
    @Select("SELECT r.role_name " +
            "FROM t_auth_role_dept rd " +
            "INNER JOIN t_auth_role r ON r.id = rd.role_id AND r.del_flag = 0 " +
            "WHERE rd.dept_id = #{deptId} AND rd.del_flag = 0 " +
            "ORDER BY rd.id ASC LIMIT 1")
    String selectFirstRoleNameByDeptId(@Param("deptId") Long deptId);

    /**
     * 根据角色ID逻辑删除部门授权关系。
     */
    @Update("UPDATE t_auth_role_dept SET del_flag = id WHERE role_id = #{roleId} AND del_flag = 0")
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据角色ID和部门ID列表逻辑删除指定授权关系。
     */
    @Update("<script>" +
            "UPDATE t_auth_role_dept SET del_flag = id " +
            "WHERE role_id = #{roleId} AND del_flag = 0 AND dept_id IN " +
            "<foreach item='deptId' collection='deptIds' open='(' separator=',' close=')'>" +
            "#{deptId}" +
            "</foreach>" +
            "</script>")
    int deleteByRoleIdAndDeptIds(@Param("roleId") Long roleId, @Param("deptIds") List<Long> deptIds);

    /**
     * 根据角色ID列表批量逻辑删除部门授权关系。
     */
    @Update("<script>" +
            "UPDATE t_auth_role_dept SET del_flag = id " +
            "WHERE role_id IN " +
            "<foreach item='roleId' collection='roleIds' open='(' separator=',' close=')'>" +
            "#{roleId}" +
            "</foreach> AND del_flag = 0" +
            "</script>")
    int deleteByRoleIds(@Param("roleIds") List<Long> roleIds);
}
