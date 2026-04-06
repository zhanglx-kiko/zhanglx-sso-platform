package com.zhanglx.sso.auth.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.zhanglx.sso.auth.domain.dto.RoleDTO;
import com.zhanglx.sso.auth.domain.dto.RolePermissionRelationshipMappingDTO;
import com.zhanglx.sso.auth.domain.po.RolePO;
import com.zhanglx.sso.auth.domain.po.RolePermissionRelationshipMappingPO;
import com.zhanglx.sso.auth.domain.po.UserRoleRelationshipMappingPO;
import com.zhanglx.sso.auth.domain.vo.RoleInfoVO;
import com.zhanglx.sso.auth.event.RolePermissionChangedEvent;
import com.zhanglx.sso.auth.event.RoleUsersChangedEvent;
import com.zhanglx.sso.auth.mapper.RoleMapper;
import com.zhanglx.sso.auth.mapper.RolePermissionRelationshipMappingMapper;
import com.zhanglx.sso.auth.mapper.UserRoleRelationshipMappingMapper;
import com.zhanglx.sso.auth.service.PermissionService;
import com.zhanglx.sso.auth.service.RoleService;
import com.zhanglx.sso.auth.utils.IRoleMapper;
import com.zhanglx.sso.core.domain.page.PageQuery;
import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.core.exception.CommonErrorCode;
import com.zhanglx.sso.core.utils.AssertUtils;
import com.zhanglx.sso.core.utils.collection.CollectionDiffUtils;
import com.zhanglx.sso.core.utils.collection.CollectionUtils;
import com.zhanglx.sso.mybatis.query.LambdaQueryWrapperX;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Strings;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/18 11:29
 * @ClassName: RoleServiceImpl
 * @Description:
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final PermissionService permissionService;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRoleRelationshipMappingMapper userRoleRelationshipMappingMapper;
    private final RolePermissionRelationshipMappingMapper rolePermissionRelationshipMappingMapper;

    @Override
    public RoleDTO addRole(RoleDTO roleDTO) {
        if (roleMapper.exists(new LambdaQueryWrapperX<RolePO>().eq(RolePO::getRoleName, roleDTO.getRoleName())
                .or().eq(RolePO::getRoleCode, roleDTO.getRoleCode()))) {
            throw new BusinessException(CommonErrorCode.CONFLICT);
        }

        RolePO role = IRoleMapper.INSTANCE.toPO(roleDTO);
        roleMapper.insert(role);
        return IRoleMapper.INSTANCE.toDTO(role);
    }

    @Override
    public RoleDTO loadRole(Long roleId) {
        AssertUtils.notNull(roleId, "role.id.cannot.be.blank");
        RolePO rolePO = roleMapper.selectById(roleId);
        AssertUtils.notNull(rolePO, CommonErrorCode.NOT_FOUND);
        RoleDTO roleDTO = IRoleMapper.INSTANCE.toDTO(rolePO);
        roleDTO.setRolePermissions(permissionService.selPermissionByRoleId(roleId));
        return roleDTO;
    }

    @Override
    public RoleInfoVO selectRoleDetail(Long roleId) {
        AssertUtils.notNull(roleId, "role.id.cannot.be.blank");
        RolePO rolePO = roleMapper.selectById(roleId);
        AssertUtils.notNull(rolePO, CommonErrorCode.NOT_FOUND);
        RoleInfoVO roleVO = IRoleMapper.INSTANCE.toVO(rolePO);
        roleVO.setUserIds(userRoleRelationshipMappingMapper.selUserIdListByRoleId(roleId));
        return roleVO;
    }

    @Override
    public RoleInfoVO bindUsers(Long roleId, List<Long> userIds) {
        AssertUtils.notNull(roleId, "business.data.invalid");

        RolePO role = roleMapper.selectById(roleId);
        AssertUtils.notNull(role, CommonErrorCode.NOT_FOUND);

        // 传入的用户列表为空时，直接清空该角色下的所有用户关联
        if (CollectionUtils.isEmpty(userIds)) {
            userRoleRelationshipMappingMapper.deleteByRoleId(roleId);

            return IRoleMapper.INSTANCE.toVO(role);
        }

        // 2. 查询当前角色已绑定的用户 ID 列表
        List<Long> existingUserIds = userRoleRelationshipMappingMapper.selectList(
                new LambdaQueryWrapperX<UserRoleRelationshipMappingPO>()
                        .eq(UserRoleRelationshipMappingPO::getRoleId, roleId)
        ).stream().map(UserRoleRelationshipMappingPO::getUserId).toList();

        // 3. 计算新旧数据的差异
        CollectionDiffUtils.DiffResult<Long> diff = CollectionDiffUtils.compare(existingUserIds, userIds);

        // 无变更，直接放行
        if (!diff.hasChanges()) {
            return IRoleMapper.INSTANCE.toVO(role);
        }

        // 4. 批量删除被移出该角色的用户关联
        Set<Long> toDeleteIds = diff.toDelete();
        if (!toDeleteIds.isEmpty()) {
            userRoleRelationshipMappingMapper.deleteByRoleIdAndUserIds(roleId, toDeleteIds.stream().toList());
        }

        // 5. 批量插入新加入该角色的用户关联
        Set<Long> toAddIds = diff.toAdd();
        if (!toAddIds.isEmpty()) {
            List<UserRoleRelationshipMappingPO> insertList = toAddIds.stream()
                    .map(userId -> {
                        UserRoleRelationshipMappingPO up = new UserRoleRelationshipMappingPO();
                        up.setUserId(userId);
                        up.setRoleId(roleId);
                        return up;
                    })
                    .toList();

            userRoleRelationshipMappingMapper.insert(insertList, 50);
        }

        // 6. 事务即将提交，发布领域事件（可用于通知 Sa-Token 清理相关用户的角色/权限缓存，踢人下线等）
        if (eventPublisher != null) {
            eventPublisher.publishEvent(new RoleUsersChangedEvent(roleId, diff));
        }

        return IRoleMapper.INSTANCE.toVO(role);
    }

    @Override
    public RoleDTO associatePermissions(Long roleId, List<RolePermissionRelationshipMappingDTO> permissions) {
        AssertUtils.notNull(roleId, "business.data.invalid");
        RolePO roleResultPO = roleMapper.selectById(roleId);
        AssertUtils.notNull(roleResultPO, CommonErrorCode.NOT_FOUND);

        if (CollectionUtils.isEmpty(permissions)) {
            // 删除所有关联关系
            rolePermissionRelationshipMappingMapper.deleteByRoleId(roleId);
            return IRoleMapper.INSTANCE.toDTO(roleResultPO);
        }

        // 1. 查询当前角色已有的权限关联
        List<Long> existingPermissionIds = rolePermissionRelationshipMappingMapper.selectList(
                new LambdaQueryWrapperX<RolePermissionRelationshipMappingPO>()
                        .eq(RolePermissionRelationshipMappingPO::getRoleId, roleId)
        ).stream().map(RolePermissionRelationshipMappingPO::getPermissionId).toList();

        // 待添加的权限ID
        List<Long> newPermissionIds = permissions.stream().map(RolePermissionRelationshipMappingDTO::getPermissionId).toList();

        // 2. 计算差异
        CollectionDiffUtils.DiffResult<Long> diff = CollectionDiffUtils.compare(existingPermissionIds, newPermissionIds);

        if (!diff.hasChanges()) {
            // 无变更，直接放行
            return IRoleMapper.INSTANCE.toDTO(roleResultPO);
        }

        // 3. 批量删除失效的关联
        Set<Long> toDeleteIds = diff.toDelete();
        if (!toDeleteIds.isEmpty()) {
            rolePermissionRelationshipMappingMapper.deleteByRoleIdAndPermissionIds(roleId, toDeleteIds.stream().toList());
        }

        // 4. 批量插入新增的关联
        Set<Long> toAddIds = diff.toAdd();
        if (!toAddIds.isEmpty()) {
            List<RolePermissionRelationshipMappingPO> insertList = toAddIds.stream()
                    .map(permissionId -> {
                        RolePermissionRelationshipMappingPO rp = new RolePermissionRelationshipMappingPO();
                        rp.setRoleId(roleId);
                        rp.setPermissionId(permissionId);
                        return rp;
                    })
                    .toList();

            // 务必确保 application.yml 或 jdbc url 中开启了 rewriteBatchedStatements=true
            rolePermissionRelationshipMappingMapper.insert(insertList);
        }

        // 5. 事务即将提交，发布领域事件通知 Sa-Token 清理缓存
        eventPublisher.publishEvent(new RolePermissionChangedEvent(roleId));

        return IRoleMapper.INSTANCE.toDTO(roleResultPO);
    }

    @Override
    public RoleDTO updateRole(Long id, RoleDTO roleDTO) {
        AssertUtils.isTrue(roleMapper.exists(new LambdaQueryWrapperX<RolePO>().eq(RolePO::getId, id)), CommonErrorCode.NOT_FOUND);
        RolePO updateRole = IRoleMapper.INSTANCE.toPO(roleDTO);
        roleMapper.updateById(updateRole);
        return IRoleMapper.INSTANCE.toDTO(updateRole);
    }

    @Override
    public RoleDTO delRole(Long id) {
        RolePO role = null;
        if (id == null || (role = roleMapper.selectById(id)) == null) {
            throw new BusinessException(CommonErrorCode.NOT_FOUND);
        }

        roleMapper.deleteByIdWithFill(id);
        userRoleRelationshipMappingMapper.deleteByRoleId(id);
        permissionService.delMappingByRoleId(Collections.singletonList(id));
        return IRoleMapper.INSTANCE.toDTO(role);
    }

    @Override
    public Page<RoleDTO> selRole(PageQuery queryParam) {
        Page<RolePO> page = Page.of(queryParam.getPageNum(), queryParam.getPageSize());

        LambdaQueryWrapperX<RolePO> wrapper = new LambdaQueryWrapperX<>();
        // 内置角色不被查询
        wrapper.like(RolePO::getRoleCode, queryParam.getSearchKey())
                .or()
                .like(RolePO::getRoleName, queryParam.getSearchKey())
                .orderByDesc(RolePO::getCreateTime);

        roleMapper.selectPage(page, wrapper);

        Page<RoleDTO> result = new Page<>();
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(IRoleMapper.INSTANCE.toDTOList(page.getRecords()));
        return result;
    }

    @Override
    public void batchDelRole(List<Long> idList) {
        AssertUtils.notEmpty(idList = idList.stream().filter(Objects::nonNull).toList(), "business.data.invalid");

        List<RolePO> existRoles = roleMapper.selectByIds(idList);
        if (CollectionUtils.isEmpty(existRoles)) {
            return;
        }

        List<Long> existIds = existRoles.stream()
                .map(RolePO::getId)
                .collect(Collectors.toList());

        roleMapper.deleteByIdsWithFill(existIds);

        userRoleRelationshipMappingMapper.deleteByRoleIds(existIds);

        permissionService.delMappingByRoleId(existIds);
    }

    @Override
    public List<RoleDTO> selectRolesForUser(String userAccount) {
        AssertUtils.notBlank(userAccount, "business.data.invalid");
        // CI 忽略大小写
        // CS 大小写敏感
        if (Strings.CI.equals("guest_username", userAccount)) {
            return Lists.newArrayList(RoleDTO.builder()
                    .roleName("guest")
                    .roleCode("role_guest")
                    .build());
        }

        List<RolePO> roles = roleMapper.selectRolesForUser(userAccount);
        if (CollectionUtils.isNotEmpty(roles)) {
            return IRoleMapper.INSTANCE.toDTOList(roles);
        }

        return Lists.newArrayList();
    }

    @Override
    public List<RoleDTO> selectRolesForUser(Long userId) {
        AssertUtils.notNull(userId, "business.data.invalid");
        // 1. 查询用户关联的角色 ID 列表
        List<Long> roleIds = userRoleRelationshipMappingMapper.selRoleIdsByUserId(userId);

        if (CollectionUtils.isEmpty(roleIds)) {
            return Lists.newArrayList();
        }

        // 2. 根据角色 ID 列表查询角色信息
        List<RolePO> roles = roleMapper.selectByIds(roleIds);

        if (CollectionUtils.isNotEmpty(roles)) {
            return IRoleMapper.INSTANCE.toDTOList(roles);
        }

        return Lists.newArrayList();
    }

}
