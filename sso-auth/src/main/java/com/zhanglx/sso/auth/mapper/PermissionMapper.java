package com.zhanglx.sso.auth.mapper;

import com.zhanglx.sso.auth.domain.po.PermissionPO;
import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.auth.enums.PermissionTypeEnum;
import com.zhanglx.sso.auth.enums.YesNoEnum;
import com.zhanglx.sso.mybatis.handler.AutoEnumTypeHandler;
import com.zhanglx.sso.mybatis.mapper.IBaseMapperX;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.session.ResultHandler;

import java.util.List;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/17 15:04
 * @ClassName: PermissionMapper
 * @Description: 权限数据访问层
 */
@Mapper
public interface PermissionMapper extends IBaseMapperX<PermissionPO> {

    /**
     * 以流式方式遍历所有权限节点。
     *
     * <p>适用于权限树构建和大批量导出场景，可避免一次性加载全量权限数据导致的内存压力。</p>
     *
     * @param handler MyBatis 流式结果处理器，每读取一行会回调一次
     */
    @Results(id = "permissionPoResultMap", value = {
            @Result(property = "isFrame", column = "is_frame", javaType = YesNoEnum.class,
                    typeHandler = AutoEnumTypeHandler.class),
            @Result(property = "type", column = "type", javaType = PermissionTypeEnum.class,
                    typeHandler = AutoEnumTypeHandler.class),
            @Result(property = "status", column = "status", javaType = EnableStatusEnum.class,
                    typeHandler = AutoEnumTypeHandler.class)
    })
    @Select("SELECT /*+ USE_INDEX(t_auth_permission idx_parent_display) */ " +
            "id, name, identification, parent_id, identity_lineage, com_path, path, " +
            "icon_str, display_no, is_frame, type, status, remark, del_flag, create_by, create_time, update_by, update_time " +
            "FROM t_auth_permission " +
            "WHERE del_flag = 0 " +
            "ORDER BY parent_id ASC, display_no ASC")
    @Options(resultSetType = ResultSetType.FORWARD_ONLY, fetchSize = Integer.MIN_VALUE)
    @ResultType(PermissionPO.class)
    void streamAllPermissions(ResultHandler<PermissionPO> handler);

    /**
     * 按用户、权限标识和权限类型查询用户拥有的权限列表。
     *
     * <p>该方法会通过用户角色映射和角色权限映射进行联表查询，支持按模块标识和权限类型做可选过滤。</p>
     *
     * @param userId 用户 ID
     * @param moduleIdentities 需要过滤的权限标识列表，可为空
     * @param permissionTypes 需要过滤的权限类型列表，可为空
     * @return 当前用户命中的权限列表
     */
    @ResultMap("permissionPoResultMap")
    @Select("<script>" +
            "SELECT DISTINCT p.id, p.name, p.identification, p.parent_id, p.identity_lineage, " +
            "       p.com_path, p.path, p.icon_str, p.display_no, p.is_frame, p.type, p.status, p.remark " +
            "FROM t_auth_user_role urm " +
            "INNER JOIN t_auth_role_permission rpm ON urm.role_id = rpm.role_id " +
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
                                                       @Param("permissionTypes") List<PermissionTypeEnum> permissionTypes);

    /**
     * 根据角色 ID 查询角色已绑定的权限列表。
     *
     * @param roleId 角色 ID
     * @return 角色已绑定的权限列表
     */
    @ResultMap("permissionPoResultMap")
    @Select("SELECT p.id, p.name, p.identification, p.parent_id, p.identity_lineage, " +
            "p.com_path, p.path, p.icon_str, p.display_no, p.is_frame, p.type, p.status, p.remark " +
            "FROM t_auth_role_permission rpm " +
            "INNER JOIN t_auth_permission p ON rpm.permission_id = p.id " +
            "WHERE rpm.role_id = #{roleId} " +
            "  AND p.del_flag = 0 " +
            "  AND rpm.del_flag = 0 " +
            "ORDER BY p.identity_lineage ASC, p.display_no ASC")
    List<PermissionPO> selPermissionByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据父级权限标识查询其所有子孙权限。
     *
     * <p>该查询基于 {@code identityLineage} 前缀匹配，常用于权限标识变更后的级联更新。</p>
     *
     * @param parentIdentity 父级权限标识
     * @return 子孙权限列表
     */
    @ResultMap("permissionPoResultMap")
    @Select("SELECT id, name, identification, parent_id, identity_lineage, com_path, path, " +
            "icon_str, display_no, is_frame, type, status, remark " +
            "FROM t_auth_permission " +
            "WHERE identity_lineage LIKE CONCAT(#{parentIdentity}, '%') " +
            "  AND del_flag = 0 " +
            "ORDER BY display_no ASC")
    List<PermissionPO> selChildrenPerm(@Param("parentIdentity") String parentIdentity);

    /**
     * 根据用户 ID 查询用户拥有的权限编码集合。
     *
     * <p>仅返回按钮和接口级权限编码，用于 Sa-Token 鉴权缓存以及前端按钮权限控制。</p>
     *
     * @param userId 用户 ID
     * @return 权限编码列表
     */
    @Select("SELECT DISTINCT p.identification " +
            "FROM t_auth_user_role urm " +
            "INNER JOIN t_auth_role_permission rpm ON urm.role_id = rpm.role_id " +
            "INNER JOIN t_auth_permission p ON rpm.permission_id = p.id " +
            "WHERE urm.user_id = #{userId} " +
            "  AND p.type >= 2 " +
            "  AND p.del_flag = 0 " +
            "  AND rpm.del_flag = 0 " +
            "  AND urm.del_flag = 0 " +
            "  AND p.identification IS NOT NULL AND p.identification != ''")
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);

}
