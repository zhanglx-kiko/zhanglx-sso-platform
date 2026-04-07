package com.zhanglx.sso.auth.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.zhanglx.sso.auth.domain.dto.RoleDTO;
import com.zhanglx.sso.auth.domain.dto.RolePermissionRelationshipMappingDTO;
import com.zhanglx.sso.auth.domain.po.AppPO;
import com.zhanglx.sso.auth.domain.po.PermissionPO;
import com.zhanglx.sso.auth.domain.po.RolePO;
import com.zhanglx.sso.auth.domain.po.RolePermissionRelationshipMappingPO;
import com.zhanglx.sso.auth.domain.po.UserPO;
import com.zhanglx.sso.auth.domain.po.UserRoleRelationshipMappingPO;
import com.zhanglx.sso.auth.domain.vo.RoleInfoVO;
import com.zhanglx.sso.auth.enums.DataScopeEnum;
import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.auth.exception.AuthOperationErrorCode;
import com.zhanglx.sso.auth.event.RolePermissionChangedEvent;
import com.zhanglx.sso.auth.event.RoleUsersChangedEvent;
import com.zhanglx.sso.auth.mapper.AppMapper;
import com.zhanglx.sso.auth.mapper.PermissionMapper;
import com.zhanglx.sso.auth.mapper.RoleDeptMapper;
import com.zhanglx.sso.auth.mapper.RoleMapper;
import com.zhanglx.sso.auth.mapper.RolePermissionRelationshipMappingMapper;
import com.zhanglx.sso.auth.mapper.UserMapper;
import com.zhanglx.sso.auth.mapper.UserRoleRelationshipMappingMapper;
import com.zhanglx.sso.auth.service.PermissionService;
import com.zhanglx.sso.auth.service.RoleService;
import com.zhanglx.sso.auth.service.support.AuthOperationGuard;
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

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class RoleServiceImpl implements RoleService {

    private final AppMapper appMapper;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final PermissionService permissionService;
    private final RoleDeptMapper roleDeptMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRoleRelationshipMappingMapper userRoleRelationshipMappingMapper;
    private final RolePermissionRelationshipMappingMapper rolePermissionRelationshipMappingMapper;
    private final AuthOperationGuard authOperationGuard;

    @Override
    public RoleDTO addRole(RoleDTO roleDTO) {
        String appCode = normalizeAppCode(roleDTO.getAppCode());
        validateApp(appCode);
        validateRoleUnique(appCode, roleDTO.getRoleCode(), roleDTO.getRoleName(), null);

        RolePO role = IRoleMapper.INSTANCE.toPO(roleDTO);
        role.setAppCode(appCode);
        if (role.getDataScope() == null) {
            role.setDataScope(DataScopeEnum.ALL.getCode());
        }
        if (role.getStatus() == null) {
            role.setStatus(EnableStatusEnum.ENABLED.getCode());
        }
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
        AssertUtils.isTrue(userList.size() == normalizedUserIds.size(), "瀛樺湪鏃犳晥鐨勭敤鎴?ID");
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
        AssertUtils.notNull(exist, CommonErrorCode.NOT_FOUND);

        String appCode = normalizeAppCode(roleDTO.getAppCode() == null ? exist.getAppCode() : roleDTO.getAppCode());
        validateApp(appCode);
        validateRoleUnique(appCode, roleDTO.getRoleCode(), roleDTO.getRoleName(), id);

        exist.setAppCode(appCode);
        exist.setRoleCode(roleDTO.getRoleCode());
        exist.setRoleName(roleDTO.getRoleName());
        exist.setDataScope(roleDTO.getDataScope());
        exist.setStatus(roleDTO.getStatus());
        exist.setRemark(roleDTO.getRemark());
        roleMapper.updateById(exist);

        if (!DataScopeEnum.CUSTOM.matches(exist.getDataScope())) {
            roleDeptMapper.deleteByRoleId(id);
        }
        return IRoleMapper.INSTANCE.toDTO(exist);
    }

    @Override
    public RoleDTO delRole(Long id) {
        RolePO role = roleMapper.selectById(id);
        AssertUtils.notNull(role, CommonErrorCode.NOT_FOUND);
        ensureCurrentUserRoleUnaffected(Collections.singleton(id), AuthOperationErrorCode.DELETE_CURRENT_USER_ROLE_FORBIDDEN);

        roleMapper.deleteByIdWithFill(id);
        userRoleRelationshipMappingMapper.deleteByRoleId(id);
        roleDeptMapper.deleteByRoleId(id);
        permissionService.delMappingByRoleId(Collections.singletonList(id));
        return IRoleMapper.INSTANCE.toDTO(role);
    }

    @Override
    public RoleDTO associatePermissions(Long roleId, List<RolePermissionRelationshipMappingDTO> permissions) {
        AssertUtils.notNull(roleId, "business.data.invalid");
        RolePO roleResultPO = roleMapper.selectById(roleId);
        AssertUtils.notNull(roleResultPO, CommonErrorCode.NOT_FOUND);

        if (CollectionUtils.isEmpty(permissions)) {
            ensureCurrentUserRoleUnaffected(Collections.singleton(roleId),
                    AuthOperationErrorCode.REDUCE_CURRENT_USER_ROLE_PERMISSION_FORBIDDEN);
            rolePermissionRelationshipMappingMapper.deleteByRoleId(roleId);
            return IRoleMapper.INSTANCE.toDTO(roleResultPO);
        }

        List<Long> newPermissionIds = permissions.stream()
                .map(RolePermissionRelationshipMappingDTO::getPermissionId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<PermissionPO> permissionList = permissionMapper.selectByIds(newPermissionIds);
        AssertUtils.isTrue(permissionList.size() == newPermissionIds.size(), "瀛樺湪鏃犳晥鐨勬潈闄?ID");

        List<Long> existingPermissionIds = rolePermissionRelationshipMappingMapper.selectList(
                new LambdaQueryWrapperX<RolePermissionRelationshipMappingPO>()
                        .eq(RolePermissionRelationshipMappingPO::getRoleId, roleId)
        ).stream().map(RolePermissionRelationshipMappingPO::getPermissionId).toList();

        CollectionDiffUtils.DiffResult<Long> diff = CollectionDiffUtils.compare(existingPermissionIds, newPermissionIds);
        if (!diff.hasChanges()) {
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
                            .build()
            ));
            rolePermissionRelationshipMappingMapper.insert(insertList);
        }

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
        AssertUtils.notEmpty(idList = idList.stream().filter(Objects::nonNull).distinct().toList(), "business.data.invalid");
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
        AssertUtils.notBlank(userAccount, "business.data.invalid");
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
        AssertUtils.notNull(userId, "business.data.invalid");
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
    public RoleDTO updateStatus(Long roleId, Integer status) {
        RolePO role = roleMapper.selectById(roleId);
        AssertUtils.notNull(role, CommonErrorCode.NOT_FOUND);
        if (EnableStatusEnum.isDisabled(status)) {
            ensureCurrentUserRoleUnaffected(Collections.singleton(roleId), AuthOperationErrorCode.DISABLE_CURRENT_USER_ROLE_FORBIDDEN);
        }
        role.setStatus(status);
        roleMapper.updateById(role);
        return IRoleMapper.INSTANCE.toDTO(role);
    }

    private void validateApp(String appCode) {
        AppPO app = appMapper.selectOne(AppPO::getAppCode, appCode);
        AssertUtils.notNull(app, "鎵€灞炲簲鐢ㄤ笉瀛樺湪");
        AssertUtils.isTrue(EnableStatusEnum.isEnabled(app.getStatus()), "鎵€灞炲簲鐢ㄥ凡鍋滅敤");
    }

    private String normalizeAppCode(String appCode) {
        return appCode == null || appCode.isBlank() ? "sso" : appCode.trim();
    }

    private void validateRoleUnique(String appCode, String roleCode, String roleName, Long excludeId) {
        AssertUtils.notBlank(roleCode, "role.code.cannot.be.blank");
        AssertUtils.notBlank(roleName, "role.name.cannot.be.blank");

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
        AssertUtils.isTrue(roleMapper.selectCount(codeWrapper) == 0, "瑙掕壊缂栫爜宸插瓨鍦?");
        AssertUtils.isTrue(roleMapper.selectCount(nameWrapper) == 0, "瑙掕壊鍚嶇О宸插瓨鍦?");
    }

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
}
