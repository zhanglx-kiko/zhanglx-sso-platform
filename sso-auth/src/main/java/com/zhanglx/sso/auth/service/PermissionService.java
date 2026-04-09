package com.zhanglx.sso.auth.service;

import com.zhanglx.sso.auth.domain.dto.PermissionDTO;
import com.zhanglx.sso.auth.domain.vo.PermissionVO;
import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.auth.enums.PermissionTypeEnum;

import java.util.List;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/10 20:49
 * @ClassName: PermissionService
 * @Description: 权限管理服务接口
 * <p>
 * 主要职责：
 * 1. 权限项基本信息管理（增删改查）
 * 2. 权限树形结构构建
 * 3. 权限导入导出（异步批量处理）
 * 4. 角色权限关联管理
 */
public interface PermissionService {

    /**
     * 新增权限项
     * <p>
     * 业务逻辑：
     * 1. 校验权限标识全局唯一性
     * 2. 如果有父级权限，构建血缘关系（identityLineage）
     * 3. 保存到数据库
     * 4. 清理权限树缓存
     *
     * @param permissionDTO 新增的权限项内容
     * @return PermissionDTO 保存后的权限项
     */
    PermissionDTO addPermission(PermissionDTO permissionDTO);

    /**
     * 删除权限项
     * <p>
     * 业务逻辑：
     * 1. 校验权限项是否存在
     * 2. 递归删除该权限下的所有子权限
     * 3. 删除权限与角色的关联关系
     * 4. 清理权限树缓存
     *
     * @param id 权限项 id
     * @return PermissionDTO 被删除的权限项
     */
    PermissionDTO delPermission(Long id);

    /**
     * 更新权限项
     * <p>
     * 业务逻辑：
     * 1. 校验权限项是否存在
     * 2. 校验新标识的唯一性（排除自身）
     * 3. 更新权限信息
     * 4. 如果标识变更，递归更新子权限的血缘关系
     * 5. 清理权限树缓存
     *
     * @param id            主键
     * @param permissionDTO 更新的内容
     * @return PermissionDTO 更新后的权限项
     */
    PermissionDTO updatePermission(Long id, PermissionDTO permissionDTO);

    /**
     * 查询权限项列表（树形结构）
     * <p>
     * 业务逻辑：
     * 1. 流式查询所有权限项（避免内存溢出）
     * 2. 如果有搜索关键字，过滤匹配的结果
     * 3. 去重处理（避免父子节点同时满足条件时的重复）
     * 4. 构建树形结构
     * 5. 应用树枝隐藏策略
     *
     * @param searchKey 查询条件（支持名称或标识模糊匹配）
     * @return List<PermissionDTO> 权限项树形列表
     */
    List<PermissionDTO> selPermission(String searchKey);

    /**
     * 根据用户名以及权限标识查询权限项列表
     * <p>
     * 业务逻辑：
     * 1. 根据用户名查询用户信息
     * 2. 查询用户通过角色关联的所有权限
     * 3. 根据标识和类型过滤
     * 4. 转换为 VO 返回
     *
     * @param username        账号
     * @param identifications 权限项标识列表（可为空，不过滤）
     * @param permissionTypes 权限项类型列表（可为空，不过滤）
     * @return List<PermissionVO> 以传入权限项分组的权限项列表
     */
    List<PermissionVO> selPermissionByIdentification(String username, List<String> identifications, List<PermissionTypeEnum> permissionTypes);

    /**
     * 根据角色 id 查询已绑定的权限项列表
     * <p>
     * 业务逻辑：
     * 1. 查询角色关联的所有权限项
     * 2. 转换为 VO 返回
     *
     * @param roleId 角色 id
     * @return List<PermissionVO> 已绑定的权限项列表
     */
    List<PermissionVO> selPermissionByRoleId(long roleId);

    /**
     * 根据角色 id 删除角色与权限项的关联关系
     * <p>
     * 业务逻辑：
     * 1. 批量删除角色与权限的关联关系
     * 2. 清理权限树缓存
     *
     * @param roleId 角色 id 列表
     */
    void delMappingByRoleId(List<Long> roleId);

    /**
     * 批量删除权限项
     * <p>
     * 业务逻辑：
     * 1. 分组处理（每 50 个一组）
     * 2. 对每组权限，递归删除子权限
     * 3. 删除权限与角色的关联关系
     * 4. 清理权限树缓存
     *
     * @param idList 权限项 id 列表
     * @return List<PermissionDTO> 被删除的权限项列表
     */
    List<PermissionDTO> batchDelPermission(List<Long> idList);

    /**
     * 批量导入数据（异步任务，运行在虚拟线程）
     * <p>
     * 核心特性：
     * 1. 使用 Fesod 直接读取磁盘文件，不占用 JVM 内存
     * 2. 尽力而为策略：有效数据入库，无效数据记录错误原因
     * 3. DFS 记忆化算法解析血缘关系
     * 4. 编程式事务控制
     * 5. 任务完成后发布权限变更事件
     * 6. 自动清理临时文件
     *
     * @param taskId   任务 id（用于进度追踪）
     * @param tempFile 临时文件（上传的 Excel）
     */
    void executeImportTask(String taskId, java.io.File tempFile);

    /**
     * 批量导出数据（异步任务，运行在虚拟线程）
     * <p>
     * 核心特性：
     * 1. 分页查询，避免一次性加载大量数据
     * 2. 使用物理临时文件接收，杜绝 OOM
     * 3. try-with-resources 自动刷盘
     * 4. 实时进度追踪
     * 5. 自动清理临时文件
     *
     * @param taskId 任务 id（用于进度追踪）
     */
    void executeExportTask(String taskId);

    /**
     * 根据用户 ID 查询用户拥有的所有权限标识集合（用于权限校验与缓存）
     * <p>
     * 业务逻辑：
     * 1. 根据用户角色映射表和角色权限映射表，关联查出所有权限
     * 2. 返回当前用户已获授权的全部 identification，供鉴权缓存与树形场景共用
     *
     * @param userId 用户 ID
     * @return List<String> 权限标识列表 (如 "user:add", "role:edit")
     */
    List<String> selectPermissionCodesByUserId(Long userId);

    PermissionDTO getPermission(Long id);

    PermissionDTO updateStatus(Long id, EnableStatusEnum status);

}
