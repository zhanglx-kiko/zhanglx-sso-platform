package com.zhanglx.sso.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.RoleDTO;
import com.zhanglx.sso.auth.domain.dto.RolePermissionRelationshipMappingDTO;
import com.zhanglx.sso.auth.domain.vo.RoleInfoVO;
import com.zhanglx.sso.core.domain.page.PageQuery;

import java.util.List;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/18 09:29
 * @ClassName: RoleService
 * @Description: 角色管理服务接口
 * <p>
 * 主要职责：
 * 1. 角色基本信息管理（增删改查）
 * 2. 角色与用户关联管理
 * 3. 角色与权限关联管理
 * 4. 用户角色查询
 */
public interface RoleService {

    /**
     * 新增角色信息
     * <p>
     * 业务逻辑：
     * 1. 校验角色名称和角色编码唯一性
     * 2. 转换为 PO 对象
     * 3. 保存到数据库
     *
     * @param roleDTO 角色信息对象
     * @return RoleDTO 保存后的角色信息
     */
    RoleDTO addRole(RoleDTO roleDTO);

    /**
     * 获取角色详情
     * <p>
     * 业务逻辑：
     * 1. 查询角色基本信息
     * 2. 查询该角色关联的所有权限项
     * 3. 组装返回结果
     *
     * @param roleId 角色 ID
     * @return RoleDTO 角色详情（包含权限列表）
     */
    RoleDTO loadRole(Long roleId);

    /**
     * 分配用户时获取角色详情
     * <p>
     * 业务逻辑：
     * 1. 查询角色基本信息
     * 2. 查询已绑定到该角色的所有用户 ID 列表
     * 3. 组装返回结果
     *
     * @param roleId 角色 ID
     * @return RoleInfoVO 角色详情（包含关联用户 ID 列表）
     */
    RoleInfoVO selectRoleDetail(Long roleId);

    /**
     * 添加用户角色关联关系
     * <p>
     * 业务逻辑：
     * 1. 校验角色是否存在
     * 2. 如果传入的用户列表为空，清空该角色所有用户关联
     * 3. 查询当前角色已绑定的用户 ID 列表
     * 4. 计算新旧数据的差异（新增、移除）
     * 5. 批量删除被移除的用户关联
     * 6. 批量插入新增的用户关联
     * 7. 发布领域事件（清理相关用户的权限缓存）
     *
     * @param roleId  角色 id
     * @param userIds 用户 id 列表
     * @return RoleInfoVO 角色信息（包含更新后的用户 ID 列表）
     */
    RoleInfoVO bindUsers(Long roleId, List<Long> userIds);

    /**
     * 修改角色信息
     * <p>
     * 业务逻辑：
     * 1. 校验角色是否存在
     * 2. 更新角色信息
     *
     * @param id      主键
     * @param roleDTO 修改后的角色信息对象
     * @return RoleDTO 更新后的角色信息
     */
    RoleDTO updateRole(Long id, RoleDTO roleDTO);

    /**
     * 根据 id 删除角色信息
     * <p>
     * 业务逻辑：
     * 1. 校验角色是否存在
     * 2. 删除角色记录
     * 3. 删除该角色与用户的关联关系
     * 4. 删除该角色与权限的关联关系
     *
     * @param id 角色 id
     * @return RoleDTO 被删除的角色信息
     */
    RoleDTO delRole(Long id);

    /**
     * 赋予权限项
     * <p>
     * 业务逻辑：
     * 1. 校验角色是否存在
     * 2. 如果权限列表为空，清空该角色所有权限关联
     * 3. 查询当前角色已有的权限 ID 列表
     * 4. 计算新旧数据的差异（新增、移除）
     * 5. 批量删除被移除的权限关联
     * 6. 批量插入新增的权限关联
     * 7. 发布领域事件（清理相关用户的权限缓存）
     *
     * @param roleId      角色 id
     * @param permissions 权限项列表
     * @return RoleDTO 角色信息
     */
    RoleDTO associatePermissions(Long roleId, List<RolePermissionRelationshipMappingDTO> permissions);

    /**
     * 分页查询角色信息
     * <p>
     * 业务逻辑：
     * 1. 构建动态查询条件（角色名称、角色编码模糊查询）
     * 2. 排除内置角色（build_in=1）
     * 3. 按创建时间倒序排序
     * 4. 转换为 DTO 返回
     *
     * @param queryParam 角色查询分页参数
     * @return Page<RoleDTO> 角色列表
     */
    Page<RoleDTO> selRole(PageQuery queryParam);

    /**
     * 批量删除角色
     * <p>
     * 业务逻辑：
     * 1. 过滤无效的 ID
     * 2. 批量删除角色记录
     * 3. 批量删除角色与用户的关联关系
     * 4. 批量删除角色与权限的关联关系
     *
     * @param idList 角色 id 列表
     */
    void batchDelRole(List<Long> idList);

    /**
     * 获取用户账户授权的角色列表
     * <p>
     * 特殊逻辑：
     * - 如果是游客账号（guest_username），返回固定的游客角色
     * - 否则查询数据库获取角色列表
     *
     * @param userAccount 用户名
     * @return List<RoleDTO> 角色列表
     */
    List<RoleDTO> selectRolesForUser(String userAccount);

    /**
     * 根据用户 ID 查询用户关联的角色列表
     * <p>
     * 业务逻辑：
     * 1. 查询用户关联的角色 ID 列表
     * 2. 根据角色 ID 列表查询角色信息
     * 3. 转换为 DTO 返回
     *
     * @param userId 用户 ID
     * @return List<RoleDTO> 角色列表
     */
    List<RoleDTO> selectRolesForUser(Long userId);

    RoleDTO updateStatus(Long roleId, Integer status);

}
