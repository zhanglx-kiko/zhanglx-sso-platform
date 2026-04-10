package com.zhanglx.sso.auth.mapper;

import com.zhanglx.sso.auth.domain.po.DeptPO;
import com.zhanglx.sso.mybatis.mapper.IBaseMapperX;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 部门数据访问层。
 */
@Mapper
public interface DeptMapper extends IBaseMapperX<DeptPO> {

    /**
     * 根据父级ID统计子部门数量。
     */
    @Select("SELECT COUNT(1) FROM t_auth_dept WHERE parent_id = #{parentId} AND del_flag = 0")
    Long countChildren(@Param("parentId") Long parentId);

    /**
     * 根据部门ID统计关联用户数量。
     */
    @Select("SELECT COUNT(1) FROM t_sys_user WHERE dept_id = #{deptId} AND del_flag = 0")
    Long countUsers(@Param("deptId") Long deptId);

    /**
     * 查询第一个子部门名称。
     */
    @Select("SELECT dept_name FROM t_auth_dept WHERE parent_id = #{parentId} AND del_flag = 0 ORDER BY id ASC LIMIT 1")
    String selectFirstChildDeptName(@Param("parentId") Long parentId);

    /**
     * 查询第一个占用部门的用户账号。
     */
    @Select("SELECT username FROM t_sys_user WHERE dept_id = #{deptId} AND del_flag = 0 ORDER BY id ASC LIMIT 1")
    String selectFirstUsernameByDeptId(@Param("deptId") Long deptId);
}
