package com.zhanglx.sso.auth.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.zhanglx.sso.auth.domain.dto.RoleDTO;
import com.zhanglx.sso.auth.domain.dto.RolePermissionRelationshipMappingDTO;
import com.zhanglx.sso.auth.domain.po.*;
import com.zhanglx.sso.auth.domain.vo.RoleInfoVO;
import com.zhanglx.sso.auth.enums.DataScopeEnum;
import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.auth.event.RolePermissionChangedEvent;
import com.zhanglx.sso.auth.event.RoleUsersChangedEvent;
import com.zhanglx.sso.auth.exception.AuthManageErrorCode;
import com.zhanglx.sso.auth.exception.AuthOperationErrorCode;
import com.zhanglx.sso.auth.mapper.*;
import com.zhanglx.sso.auth.service.PermissionService;
import com.zhanglx.sso.auth.service.RoleService;
import com.zhanglx.sso.auth.service.support.AuthOperationGuard;
import com.zhanglx.sso.auth.utils.IRoleMapper;
import com.zhanglx.sso.core.domain.page.PageQuery;
import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.core.utils.AssertUtils;
import com.zhanglx.sso.core.utils.collection.CollectionDiffUtils;
import com.zhanglx.sso.core.utils.collection.CollectionUtils;
import com.zhanglx.sso.mybatis.query.LambdaQueryWrapperX;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Strings;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色服务实现。
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class RoleServiceImpl implements RoleService {
    /**
     * 应用映射器。
     */
    private final AppMapper appMapper;
    /**
     * 用户映射器。
     */
    private final UserMapper userMapper;
    /**
     * 角色映射器。
     */
    private final RoleMapper roleMapper;
    /**
     * 权限映射器。
     */
    private final PermissionMapper permissionMapper;
    /**
     * 权限服务。
     */
    private final PermissionService permissionService;
    /**
     * 角色部门映射器。
     */
    private final RoleDeptMapper roleDeptMapper;
    /**
     * 事件发布器。
     */
    private final ApplicationEventPublisher eventPublisher;
    /**
     * 用户角色关系映射器。
     */
    private final UserRoleRelationshipMappingMapper userRoleRelationshipMappingMapper;
    /**
     * 角色权限关系映射器。
     */
    private final RolePermissionRelationshipMappingMapper rolePermissionRelationshipMappingMapper;
    /**
     * 操作保护组件。
     */
    private final AuthOperationGuard authOperationGuard;

    @Override
    public RoleDTO addRole(RoleDTO roleDTO) {
        String appCode = normalizeAppCode(roleDTO.getAppCode());
        validateApp(appCode);
        validateRoleUnique(appCode, roleDTO.getRoleCode(), roleDTO.getRoleName(), null);

        RolePO role = IRoleMapper.INSTANCE.toPO(roleDTO);
        role.setAppCode(appCode);
        if (role.getDataScope() == null) {
            role.setDataScope(DataScopeEnum.ALL);
        }
        if (role.getStatus() == null) {
            role.setStatus(EnableStatusEnum.ENABLED);
        }
        roleMapper.insert(role);
        return IRoleMapper.INSTANCE.toDTO(role);
    }

    @Override
    public RoleDTO loadRole(Long roleId) {
        AssertUtils.notNull(roleId, AuthManageErrorCode.ROLE_ID_REQUIRED);
        RolePO rolePO = roleMapper.selectById(roleId);
        AssertUtils.notNull(rolePO, AuthManageErrorCode.ROLE_NOT_FOUND);
        RoleDTO roleDTO = IRoleMapper.INSTANCE.toDTO(rolePO);
        roleDTO.setRolePermissions(permissionService.selPermissionByRoleId(roleId));
        return roleDTO;
    }

    @Override
    public RoleInfoVO selectRoleDetail(Long roleId) {
        AssertUtils.notNull(roleId, AuthManageErrorCode.ROLE_ID_REQUIRED);
        RolePO rolePO = roleMapper.selectById(roleId);
        AssertUtils.notNull(rolePO, AuthManageErrorCode.ROLE_NOT_FOUND);
        RoleInfoVO roleVO = IRoleMapper.INSTANCE.toVO(rolePO);
        roleVO.setUserIds(userRoleRelationshipMappingMapper.selUserIdListByRoleId(roleId));
        return roleVO;
    }

    @Override
    public RoleInfoVO bindUsers(Long roleId, List<Long> userIds) {
        AssertUtils.notNull(roleId, AuthManageErrorCode.ROLE_ID_REQUIRED);
        RolePO role = roleMapper.selectById(roleId);
        AssertUtils.notNull(role, AuthManageErrorCode.ROLE_NOT_FOUND);

        List<Long> existingUserIds = userRoleRelationshipMappingMapper.selectList(
                new LambdaQueryWrapperX<UserRoleRelationshipMappingPO>()
                        .eq(UserRoleRelationshipMappingPO::getRoleId, roleId)
        ).stream().map(UserRoleRelationshipMappingPO::getUserId).toList();

        if (CollectionUtils.isEmpty(userIds)) {
            authOperationGuard.checkRoleUsersBindingDoesNotRemoveCurrentUser(existingUserIds, List.of());
            userRoleRelationshipMappingMapper.deleteByRoleId(roleId);
            return IRoleMapper.INSTANCE.toVO(role);
        }

        List<Long> normalizedUserIds = userIds.stream().filter(Objects::nonNull).distinct().toList();
        List<UserPO> userList = userMapper.selectByIds(normalizedUserIds);
        AssertUtils.isTrue(userList.size() == normalizedUserIds.size(), AuthManageErrorCode.ROLE_USER_IDS_INVALID);
        authOperationGuard.checkRoleUsersBindingDoesNotRemoveCurrentUser(existingUserIds, normalizedUserIds);

        CollectionDiffUtils.DiffResult<Long> diff = CollectionDiffUtils.compare(existingUserIds, normalizedUserIds);
        if (!diff.hasChanges()) {
            return IRoleMapper.INSTANCE.toVO(role);
        }

        Set<Long> toDeleteIds = diff.toDelete();
        if (!toDeleteIds.isEmpty()) {
            userRoleRelationshipMappingMapper.deleteByRoleIdAndUserIds(roleId, toDeleteIds.stream().toList());
        }

        Set<Long> toAddIds = diff.toAdd();
        if (!toAddIds.isEmpty()) {
            List<UserRoleRelationshipMappingPO> insertList = Lists.newArrayListWithCapacity(toAddIds.size());
            toAddIds.forEach(userId -> insertList.add(
                    UserRoleRelationshipMappingPO.builder().userId(userId).roleId(roleId).build()
            ));
            userRoleRelationshipMappingMapper.insert(insertList);
        }

        if (eventPublisher != null) {
            eventPublisher.publishEvent(new RoleUsersChangedEvent(roleId, diff));
        }
        return IRoleMapper.INSTANCE.toVO(role);
    }

    @Override
    public RoleDTO updateRole(Long id, RoleDTO roleDTO) {
        RolePO exist = roleMapper.selectById(id);
        AssertUtils.notNull(exist, AuthManageErrorCode.ROLE_NOT_FOUND);

        String appCode = normalizeAppCode(roleDTO.getAppCode() == null ? exist.getAppCode() : roleDTO.getAppCode());
        validateApp(appCode);
        validateRoleUnique(appCode, roleDTO.getRoleCode(), roleDTO.getRoleName(), id);

        exist.setAppCode(appCode);
        exist.setRoleCode(roleDTO.getRoleCode());
        exist.setRoleName(roleDTO.getRoleName());
        exist.setDataScope(roleDTO.getDataScope());
        exist.setStatus(roleDTO.getStatus());
        exist.setRemark(roleDTO.getRemark());
        RolePO updatePO = new RolePO();
        updatePO.setId(id);
        updatePO.setAppCode(appCode);
        updatePO.setRoleCode(roleDTO.getRoleCode());
        updatePO.setRoleName(roleDTO.getRoleName());
        updatePO.setDataScope(roleDTO.getDataScope());
        updatePO.setStatus(roleDTO.getStatus());
        updatePO.setRemark(roleDTO.getRemark());
        roleMapper.updateById(updatePO);

        if (!DataScopeEnum.CUSTOM.matches(exist.getDataScope())) {
            roleDeptMapper.deleteByRoleId(id);
        }
        return IRoleMapper.INSTANCE.toDTO(exist);
    }

    @Override
    public RoleDTO delRole(Long id) {
        RolePO role = roleMapper.selectById(id);
        AssertUtils.notNull(role, AuthManageErrorCode.ROLE_NOT_FOUND);
        ensureCurrentUserRoleUnaffected(Collections.singleton(id), AuthOperationErrorCode.DELETE_CURRENT_USER_ROLE_FORBIDDEN);

        roleMapper.deleteByIdWithFill(id);
        userRoleRelationshipMappingMapper.deleteByRoleId(id);
        roleDeptMapper.deleteByRoleId(id);
        permissionService.delMappingByRoleId(Collections.singletonList(id));
        return IRoleMapper.INSTANCE.toDTO(role);
    }

    @Override
    public RoleDTO associatePermissions(Long roleId, List<RolePermissionRelationshipMappingDTO> permissions) {
        AssertUtils.notNull(roleId, AuthManageErrorCode.ROLE_ID_REQUIRED);
        RolePO roleResultPO = roleMapper.selectById(roleId);
        AssertUtils.notNull(roleResultPO, AuthManageErrorCode.ROLE_NOT_FOUND);

        if (CollectionUtils.isEmpty(permissions)) {
            ensureCurrentUserRoleUnaffected(Collections.singleton(roleId),
                    AuthOperationErrorCode.REDUCE_CURRENT_USER_ROLE_PERMISSION_FORBIDDEN);
            rolePermissionRelationshipMappingMapper.deleteByRoleId(roleId);
            return IRoleMapper.INSTANCE.toDTO(roleResultPO);
        }

        Map<Long, RolePermissionRelationshipMappingDTO> permissionRequestMap = permissions.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getPermissionId() != null)
                .collect(Collectors.toMap(
                        RolePermissionRelationshipMappingDTO::getPermissionId,
                        item -> item,
                        (existing, replacement) -> replacement
                ));
        List<Long> newPermissionIds = permissionRequestMap.keySet().stream().toList();
        AssertUtils.notEmpty(newPermissionIds, AuthManageErrorCode.ROLE_PERMISSION_IDS_EMPTY);
        List<PermissionPO> permissionList = permissionMapper.selectByIds(newPermissionIds);
        AssertUtils.isTrue(permissionList.size() == newPermissionIds.size(), AuthManageErrorCode.ROLE_PERMISSION_IDS_INVALID);

        List<RolePermissionRelationshipMappingPO> existingMappings = rolePermissionRelationshipMappingMapper.selectList(
                new LambdaQueryWrapperX<RolePermissionRelationshipMappingPO>()
                        .eq(RolePermissionRelationshipMappingPO::getRoleId, roleId)
        );
        List<Long> existingPermissionIds = existingMappings.stream()
                .map(RolePermissionRelationshipMappingPO::getPermissionId)
                .toList();

        CollectionDiffUtils.DiffResult<Long> diff = CollectionDiffUtils.compare(existingPermissionIds, newPermissionIds);
        if (!diff.hasChanges()) {
            syncPermissionExpireTime(existingMappings, permissionRequestMap);
            return IRoleMapper.INSTANCE.toDTO(roleResultPO);
        }

        Set<Long> toDeleteIds = diff.toDelete();
        if (!toDeleteIds.isEmpty()) {
            ensureCurrentUserRoleUnaffected(Collections.singleton(roleId),
                    AuthOperationErrorCode.REDUCE_CURRENT_USER_ROLE_PERMISSION_FORBIDDEN);
            rolePermissionRelationshipMappingMapper.deleteByRoleIdAndPermissionIds(roleId, toDeleteIds.stream().toList());
        }

        Set<Long> toAddIds = diff.toAdd();
        if (!toAddIds.isEmpty()) {
            List<RolePermissionRelationshipMappingPO> insertList = Lists.newArrayListWithCapacity(toAddIds.size());
            toAddIds.forEach(permissionId -> insertList.add(
                    RolePermissionRelationshipMappingPO.builder()
                            .roleId(roleId)
                            .permissionId(permissionId)
                            .expireTime(permissionRequestMap.get(permissionId).getExpireTime())
                            .build()
            ));
            rolePermissionRelationshipMappingMapper.insert(insertList);
        }

        syncPermissionExpireTime(existingMappings, permissionRequestMap);
        eventPublisher.publishEvent(new RolePermissionChangedEvent(roleId));
        return IRoleMapper.INSTANCE.toDTO(roleResultPO);
    }

    @Override
    public Page<RoleDTO> selRole(PageQuery queryParam) {
        Page<RolePO> page = Page.of(queryParam.getPageNum(), queryParam.getPageSize());
        LambdaQueryWrapperX<RolePO> wrapper = new LambdaQueryWrapperX<RolePO>()
                .orderByDesc(RolePO::getCreateTime);

        if (queryParam != null && queryParam.getSearchKey() != null && !queryParam.getSearchKey().isBlank()) {
            wrapper.and(w -> w.like(RolePO::getRoleCode, queryParam.getSearchKey())
                    .or()
                    .like(RolePO::getRoleName, queryParam.getSearchKey())
                    .or()
                    .like(RolePO::getAppCode, queryParam.getSearchKey()));
        }

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
        AssertUtils.notEmpty(idList = idList.stream().filter(Objects::nonNull).distinct().toList(), AuthManageErrorCode.ROLE_IDS_EMPTY);
        List<RolePO> existRoles = roleMapper.selectByIds(idList);
        if (CollectionUtils.isEmpty(existRoles)) {
            return;
        }

        List<Long> existIds = existRoles.stream().map(RolePO::getId).collect(Collectors.toList());
        ensureCurrentUserRoleUnaffected(existIds, AuthOperationErrorCode.DELETE_CURRENT_USER_ROLE_FORBIDDEN);
        roleMapper.deleteByIdsWithFill(existIds);
        userRoleRelationshipMappingMapper.deleteByRoleIds(existIds);
        roleDeptMapper.deleteByRoleIds(existIds);
        permissionService.delMappingByRoleId(existIds);
    }

    @Override
    public List<RoleDTO> selectRolesForUser(String userAccount) {
        AssertUtils.notBlank(userAccount, AuthManageErrorCode.PERMISSION_USERNAME_REQUIRED);
        if (Strings.CI.equals("guest_username", userAccount)) {
            return Lists.newArrayList(RoleDTO.builder().roleName("guest").roleCode("role_guest").build());
        }

        List<RolePO> roles = roleMapper.selectRolesForUser(userAccount);
        if (CollectionUtils.isNotEmpty(roles)) {
            return IRoleMapper.INSTANCE.toDTOList(roles);
        }
        return Lists.newArrayList();
    }

    @Override
    public List<RoleDTO> selectRolesForUser(Long userId) {
        AssertUtils.notNull(userId, AuthManageErrorCode.USER_ID_REQUIRED);
        List<Long> roleIds = userRoleRelationshipMappingMapper.selRoleIdsByUserId(userId);
        if (CollectionUtils.isEmpty(roleIds)) {
            return Lists.newArrayList();
        }
        List<RolePO> roles = roleMapper.selectByIds(roleIds);
        if (CollectionUtils.isNotEmpty(roles)) {
            return IRoleMapper.INSTANCE.toDTOList(roles);
        }
        return Lists.newArrayList();
    }

    @Override
    public RoleDTO updateStatus(Long roleId, EnableStatusEnum status) {
        RolePO role = roleMapper.selectById(roleId);
        AssertUtils.notNull(role, AuthManageErrorCode.ROLE_NOT_FOUND);
        if (EnableStatusEnum.isDisabled(status)) {
            ensureCurrentUserRoleUnaffected(Collections.singleton(roleId), AuthOperationErrorCode.DISABLE_CURRENT_USER_ROLE_FORBIDDEN);
        }
        role.setStatus(status);
        RolePO updatePO = new RolePO();
        updatePO.setId(roleId);
        updatePO.setStatus(status);
        roleMapper.updateById(updatePO);
        return IRoleMapper.INSTANCE.toDTO(role);
    }

    /**
     * 校验应用配置是否可用。
     */
    private void validateApp(String appCode) {
        AppPO app = appMapper.selectOne(AppPO::getAppCode, appCode);
        AssertUtils.notNull(app, AuthManageErrorCode.APP_NOT_FOUND, appCode);
        AssertUtils.isTrue(EnableStatusEnum.isEnabled(app.getStatus()), AuthManageErrorCode.APP_DISABLED, app.getAppName());
    }

    /**
     * 规范化应用编码。
     */
    private String normalizeAppCode(String appCode) {
        return appCode == null || appCode.isBlank() ? "sso" : appCode.trim();
    }

    /**
     * 校验角色编码和名称是否唯一。
     */
    private void validateRoleUnique(String appCode, String roleCode, String roleName, Long excludeId) {
        AssertUtils.notBlank(roleCode, AuthManageErrorCode.ROLE_CODE_REQUIRED);
        AssertUtils.notBlank(roleName, AuthManageErrorCode.ROLE_NAME_REQUIRED);

        LambdaQueryWrapperX<RolePO> codeWrapper = new LambdaQueryWrapperX<RolePO>()
                .eq(RolePO::getAppCode, appCode)
                .eq(RolePO::getRoleCode, roleCode);
        LambdaQueryWrapperX<RolePO> nameWrapper = new LambdaQueryWrapperX<RolePO>()
                .eq(RolePO::getAppCode, appCode)
                .eq(RolePO::getRoleName, roleName);
        if (excludeId != null) {
            codeWrapper.ne(RolePO::getId, excludeId);
            nameWrapper.ne(RolePO::getId, excludeId);
        }
        AssertUtils.isTrue(roleMapper.selectCount(codeWrapper) == 0, AuthManageErrorCode.ROLE_CODE_ALREADY_EXISTS, roleCode);
        AssertUtils.isTrue(roleMapper.selectCount(nameWrapper) == 0, AuthManageErrorCode.ROLE_NAME_ALREADY_EXISTS, roleName);
    }

    /**
     * 确保关键约束条件成立。
     */
    private void ensureCurrentUserRoleUnaffected(java.util.Collection<Long> roleIds, AuthOperationErrorCode errorCode) {
        Long currentUserId = authOperationGuard.getCurrentLoginUserId();
        if (currentUserId == null || CollectionUtils.isEmpty(roleIds)) {
            return;
        }

        Set<Long> targetRoleIds = roleIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (targetRoleIds.isEmpty()) {
            return;
        }

        List<Long> currentUserRoleIds = userRoleRelationshipMappingMapper.selRoleIdsByUserId(currentUserId);
        if (CollectionUtils.isEmpty(currentUserRoleIds)) {
            return;
        }

        if (currentUserRoleIds.stream().anyMatch(targetRoleIds::contains)) {
            throw new BusinessException(errorCode);
        }
    }

    /**
     * 过期时间虽然当前未参与鉴权，但需要先持久化到关系表，避免后续能力上线时补历史数据。
     */
    private void syncPermissionExpireTime(List<RolePermissionRelationshipMappingPO> existingMappings,
                                          Map<Long, RolePermissionRelationshipMappingDTO> permissionRequestMap) {
        if (CollectionUtils.isEmpty(existingMappings) || permissionRequestMap == null || permissionRequestMap.isEmpty()) {
            return;
        }

        existingMappings.stream()
                .filter(Objects::nonNull)
                .filter(item -> permissionRequestMap.containsKey(item.getPermissionId()))
                .filter(item -> !Objects.equals(
                        item.getExpireTime(),
                        permissionRequestMap.get(item.getPermissionId()).getExpireTime()
                ))
                .forEach(item -> {
                    item.setExpireTime(permissionRequestMap.get(item.getPermissionId()).getExpireTime());
                    RolePermissionRelationshipMappingPO updatePO = new RolePermissionRelationshipMappingPO();
                    updatePO.setId(item.getId());
                    updatePO.setExpireTime(item.getExpireTime());
                    rolePermissionRelationshipMappingMapper.updateById(updatePO);
                });
    }
}
