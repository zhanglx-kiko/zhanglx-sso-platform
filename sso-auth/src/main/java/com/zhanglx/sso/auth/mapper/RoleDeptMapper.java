package com.zhanglx.sso.auth.mapper;

import com.zhanglx.sso.auth.domain.po.RoleDeptPO;
import com.zhanglx.sso.mybatis.mapper.IBaseMapperX;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface RoleDeptMapper extends IBaseMapperX<RoleDeptPO> {

    @Select("SELECT dept_id FROM t_auth_role_dept WHERE role_id = #{roleId} AND del_flag = 0")
    List<Long> selectDeptIdsByRoleId(@Param("roleId") Long roleId);

    @Select("SELECT COUNT(1) FROM t_auth_role_dept WHERE dept_id = #{deptId} AND del_flag = 0")
    Long countByDeptId(@Param("deptId") Long deptId);

    @Update("UPDATE t_auth_role_dept SET del_flag = id WHERE role_id = #{roleId} AND del_flag = 0")
    int deleteByRoleId(@Param("roleId") Long roleId);

    @Update("<script>" +
            "UPDATE t_auth_role_dept SET del_flag = id " +
            "WHERE role_id = #{roleId} AND del_flag = 0 AND dept_id IN " +
            "<foreach item='deptId' collection='deptIds' open='(' separator=',' close=')'>" +
            "#{deptId}" +
            "</foreach>" +
            "</script>")
    int deleteByRoleIdAndDeptIds(@Param("roleId") Long roleId, @Param("deptIds") List<Long> deptIds);

    @Update("<script>" +
            "UPDATE t_auth_role_dept SET del_flag = id " +
            "WHERE role_id IN " +
            "<foreach item='roleId' collection='roleIds' open='(' separator=',' close=')'>" +
            "#{roleId}" +
            "</foreach> AND del_flag = 0" +
            "</script>")
    int deleteByRoleIds(@Param("roleIds") List<Long> roleIds);
}
