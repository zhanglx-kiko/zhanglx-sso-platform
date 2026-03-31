package com.zhanglx.sso.auth.mapper;

import com.zhanglx.sso.auth.domain.po.PermissionPO;
import com.zhanglx.sso.mybatis.mapper.IBaseMapperX;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.session.ResultHandler;

import java.util.List;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/17 15:04
 * @ClassName: PermissionMapper
 * @Description:
 */
@Mapper
public interface PermissionMapper extends IBaseMapperX<PermissionPO> {

    /**
     * 流式获取所有权限节点
     *
     * 1. MySQL 9.6.0 下 fetchSize = Integer.MIN_VALUE 触发真正的流式读取
     * 2. 增加 USE INDEX 提示，强制使用 idx_parent_id_display_no 索引
     * 3. 避免 filesort，直接按索引顺序读取
     *
     * @param handler 结果处理器（每读到一行数据就会回调一次）
     */
    @Select("SELECT /*+ USE_INDEX(t_auth_permission idx_parent_display) */ " +
            "id, name, identification, parent_id, identity_lineage, com_path, path, " +
            "icon_str, display_no, is_frame, type, remark, del_flag, create_by, create_time, update_by, update_time " +
            "FROM t_auth_permission " +
            "WHERE del_flag = 0 " +
            "ORDER BY parent_id ASC, display_no ASC")
    @Options(resultSetType = ResultSetType.FORWARD_ONLY, fetchSize = Integer.MIN_VALUE)
    @ResultType(PermissionPO.class)
    void streamAllPermissions(ResultHandler<PermissionPO> handler);

    /**
     * 根据用户 ID 查询权限
     */
    @Select("<script>" +
            "SELECT DISTINCT p.id, p.name, p.identification, p.parent_id, p.identity_lineage, " +
            "       p.com_path, p.path, p.icon_str, p.display_no, p.is_frame, p.type, p.remark " +
            "FROM t_auth_user_role_mapping urm " +
            "INNER JOIN t_auth_role_permission_mapping rpm ON urm.role_id = rpm.role_id " +
            "INNER JOIN t_auth_permission p ON rpm.permission_id = p.id " +
            "WHERE urm.user_id = #{userId} " +
            "  AND p.del_flag = 0 " +
            "  AND rpm.del_flag = 0 " +
            "  AND urm.del_flag = 0 " +
            "<if test='moduleIdentities != null and moduleIdentities.size() > 0'>" +
            "  AND p.identification IN " +
            "<foreach item='identity' collection='moduleIdentities' open='(' separator=',' close=')'>" +
            "#{identity}" +
            "</foreach>" +
            "</if>" +
            "<if test='permissionTypes != null and permissionTypes.size() > 0'>" +
            "  AND p.type IN " +
            "<foreach item='type' collection='permissionTypes' open='(' separator=',' close=')'>" +
            "#{type}" +
            "</foreach>" +
            "</if>" +
            "ORDER BY p.identity_lineage ASC, p.display_no ASC" +
            "</script>")
    List<PermissionPO> selectByUserWithIdentityAndType(@Param("userId") Long userId,
                                                       @Param("moduleIdentities") List<String> moduleIdentities,
                                                       @Param("permissionTypes") List<String> permissionTypes);
    /**
     * 根据角色 ID 查询权限
     */
    @Select("SELECT p.id, p.name, p.identification, p.parent_id, p.identity_lineage, " +
            "p.com_path, p.path, p.icon_str, p.display_no, p.is_frame, p.type, p.remark " +
            "FROM t_auth_role_permission_mapping rpm " +
            "INNER JOIN t_auth_permission p ON rpm.permission_id = p.id " +
            "WHERE rpm.role_id = #{roleId} " +
            "  AND p.del_flag = 0 " +
            "  AND rpm.del_flag = 0 " +
            "ORDER BY p.identity_lineage ASC, p.display_no ASC")
    List<PermissionPO> selPermissionByRoleId(@Param("roleId") Long roleId);

    /**
     * 查询子权限
     */
    @Select("SELECT id, name, identification, parent_id, identity_lineage, com_path, path, " +
            "icon_str, display_no, is_frame, type, remark " +
            "FROM t_auth_permission " +
            "WHERE identity_lineage LIKE CONCAT(#{parentIdentity}, '%') " +
            "  AND del_flag = 0 " +
            "ORDER BY display_no ASC")
    List<PermissionPO> selChildrenPerm(@Param("parentIdentity") String parentIdentity);

}
