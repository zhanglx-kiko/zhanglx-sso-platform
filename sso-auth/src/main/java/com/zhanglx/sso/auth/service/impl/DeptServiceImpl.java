package com.zhanglx.sso.auth.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.DeptDTO;
import com.zhanglx.sso.auth.domain.dto.DeptQueryDTO;
import com.zhanglx.sso.auth.domain.po.DeptPO;
import com.zhanglx.sso.auth.domain.po.RoleDeptPO;
import com.zhanglx.sso.auth.domain.po.RolePO;
import com.zhanglx.sso.auth.enums.DataScopeEnum;
import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.auth.exception.AuthManageErrorCode;
import com.zhanglx.sso.auth.mapper.DeptMapper;
import com.zhanglx.sso.auth.mapper.RoleDeptMapper;
import com.zhanglx.sso.auth.mapper.RoleMapper;
import com.zhanglx.sso.auth.service.DeptService;
import com.zhanglx.sso.auth.service.support.AuthReferenceCheckSupport;
import com.zhanglx.sso.core.strategy.TreeCycleStrategy;
import com.zhanglx.sso.auth.utils.ISystemManageMapper;
import com.zhanglx.sso.core.utils.AssertUtils;
import com.zhanglx.sso.core.strategy.TreeFilterStrategy;
import com.zhanglx.sso.core.utils.tree.HighPerfTreeBuilder;
import com.zhanglx.sso.mybatis.query.LambdaQueryWrapperX;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 部门服务实现。
 */
@Service
@RequiredArgsConstructor
public class DeptServiceImpl implements DeptService {
    /**
     * 部门映射器。
     */
    private final DeptMapper deptMapper;
    /**
     * 角色映射器。
     */
    private final RoleMapper roleMapper;
    /**
     * 角色部门映射器。
     */
    private final RoleDeptMapper roleDeptMapper;
    private final AuthReferenceCheckSupport authReferenceCheckSupport;
    private final HighPerfTreeBuilder treeBuilder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeptDTO create(DeptDTO deptDTO) {
        Long parentId = normalizeParentId(deptDTO.getParentId());
        DeptPO parent = getParentOrNull(parentId);
        validateNameUnique(parentId, deptDTO.getDeptName(), null);

        DeptPO po = ISystemManageMapper.INSTANCE.toPO(deptDTO);
        po.setParentId(parentId);
        po.setAncestors(buildAncestors(parent));
        if (po.getSortNum() == null) {
            po.setSortNum(0);
        }
        if (po.getStatus() == null) {
            po.setStatus(EnableStatusEnum.ENABLED);
        }
        deptMapper.insert(po);
        return ISystemManageMapper.INSTANCE.toDTO(po);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeptDTO update(Long id, DeptDTO deptDTO) {
        DeptPO exist = getDeptOrThrow(id);
        Long parentId = normalizeParentId(deptDTO.getParentId());
        AssertUtils.isTrue(!Objects.equals(id, parentId), AuthManageErrorCode.DEPT_PARENT_CANNOT_SELF);

        DeptPO parent = getParentOrNull(parentId);
        String oldPath = buildSelfPath(exist);
        String newAncestors = buildAncestors(parent);
        AssertUtils.isFalse(isDescendantParent(id, parent), AuthManageErrorCode.DEPT_PARENT_CANNOT_BE_DESCENDANT);
        validateNameUnique(parentId, deptDTO.getDeptName(), id);

        exist.setParentId(parentId);
        exist.setAncestors(newAncestors);
        exist.setDeptName(deptDTO.getDeptName());
        exist.setSortNum(deptDTO.getSortNum());
        exist.setStatus(deptDTO.getStatus());
        DeptPO updatePO = new DeptPO();
        updatePO.setId(id);
        updatePO.setParentId(parentId);
        updatePO.setAncestors(newAncestors);
        updatePO.setDeptName(deptDTO.getDeptName());
        updatePO.setSortNum(deptDTO.getSortNum());
        updatePO.setStatus(deptDTO.getStatus());
        deptMapper.updateById(updatePO);

        String newPath = buildSelfPath(exist);
        if (!Objects.equals(oldPath, newPath)) {
            updateChildrenAncestors(oldPath, newPath);
        }
        return ISystemManageMapper.INSTANCE.toDTO(exist);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        DeptPO exist = getDeptOrThrow(id);
        authReferenceCheckSupport.ensureDeptCanDelete(id, exist.getDeptName());
        deptMapper.deleteByIdWithFill(exist.getId());
    }

    @Override
    public DeptDTO getById(Long id) {
        return ISystemManageMapper.INSTANCE.toDTO(getDeptOrThrow(id));
    }

    @Override
    public Page<DeptDTO> pageQuery(DeptQueryDTO queryDTO) {
        Page<DeptPO> page = Page.of(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapperX<DeptPO> wrapper = new LambdaQueryWrapperX<DeptPO>()
                .eqIfPresent(DeptPO::getParentId, queryDTO.getParentId())
                .likeIfPresent(DeptPO::getDeptName, queryDTO.getDeptName())
                .eqIfPresent(DeptPO::getStatus, queryDTO.getStatus())
                .orderByAsc(DeptPO::getSortNum)
                .orderByDesc(DeptPO::getCreateTime);

        if (StrUtil.isNotBlank(queryDTO.getSearchKey())) {
            wrapper.like(DeptPO::getDeptName, queryDTO.getSearchKey());
        }

        deptMapper.selectPage(page, wrapper);
        Page<DeptDTO> result = new Page<>();
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(ISystemManageMapper.INSTANCE.toDeptDTOList(page.getRecords()));
        return result;
    }

    @Override
    public List<DeptDTO> treeQuery(String deptName, Integer status) {
        EnableStatusEnum statusEnum = EnableStatusEnum.fromCode(status);
        List<DeptPO> all = deptMapper.selectList(new LambdaQueryWrapperX<DeptPO>()
                .eqIfPresent(DeptPO::getStatus, statusEnum)
                .orderByAsc(DeptPO::getSortNum)
                .orderByDesc(DeptPO::getCreateTime));
        List<DeptDTO> allDtos = ISystemManageMapper.INSTANCE.toDeptDTOList(all);
        if (StrUtil.isBlank(deptName)) {
            return treeBuilder.buildTree(allDtos, TreeFilterStrategy.NO_FILTER, TreeCycleStrategy.BREAK_AND_CONTINUE, deptTreeComparator());
        }

        Map<Long, DeptDTO> dtoMap = allDtos.stream().collect(Collectors.toMap(DeptDTO::getId, Function.identity()));
        LinkedHashMap<Long, DeptDTO> matched = new LinkedHashMap<>();
        for (DeptDTO dept : allDtos) {
            if (dept.getDeptName() != null && dept.getDeptName().contains(deptName)) {
                matched.put(dept.getId(), dept);
                appendAncestors(dept, dtoMap, matched);
            }
        }
        return treeBuilder.buildTree(new ArrayList<>(matched.values()), TreeFilterStrategy.NO_FILTER, TreeCycleStrategy.BREAK_AND_CONTINUE, deptTreeComparator());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeptDTO updateStatus(Long id, EnableStatusEnum status) {
        DeptPO exist = getDeptOrThrow(id);
        if (EnableStatusEnum.isEnabled(status) && exist.getParentId() != null && exist.getParentId() > 0) {
            DeptPO parent = getDeptOrThrow(exist.getParentId());
            AssertUtils.isTrue(EnableStatusEnum.isEnabled(parent.getStatus()), AuthManageErrorCode.DEPT_PARENT_DISABLED_CANNOT_ENABLE, parent.getDeptName());
        }

        exist.setStatus(status);
        DeptPO updatePO = new DeptPO();
        updatePO.setId(id);
        updatePO.setStatus(status);
        deptMapper.updateById(updatePO);

        if (EnableStatusEnum.isDisabled(status)) {
            String currentPath = buildSelfPath(exist);
            List<DeptPO> children = deptMapper.selectList().stream()
                    .filter(item -> item.getAncestors() != null
                            && (item.getAncestors().equals(currentPath) || item.getAncestors().startsWith(currentPath + ",")))
                    .toList();
            for (DeptPO child : children) {
                child.setStatus(EnableStatusEnum.DISABLED);
                DeptPO childUpdatePO = new DeptPO();
                childUpdatePO.setId(child.getId());
                childUpdatePO.setStatus(EnableStatusEnum.DISABLED);
                deptMapper.updateById(childUpdatePO);
            }
        }
        return ISystemManageMapper.INSTANCE.toDTO(exist);
    }

    @Override
    public List<DeptDTO> listByRole(Long roleId) {
        AssertUtils.notNull(roleId, AuthManageErrorCode.ROLE_ID_REQUIRED);
        List<Long> deptIds = roleDeptMapper.selectDeptIdsByRoleId(roleId);
        if (deptIds == null || deptIds.isEmpty()) {
            return List.of();
        }
        return ISystemManageMapper.INSTANCE.toDeptDTOList(deptMapper.selectByIds(deptIds));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<DeptDTO> bindRoleDepts(Long roleId, List<Long> deptIds) {
        RolePO role = roleMapper.selectById(roleId);
        AssertUtils.notNull(role, AuthManageErrorCode.ROLE_NOT_FOUND);
        AssertUtils.isTrue(DataScopeEnum.CUSTOM.matches(role.getDataScope()), AuthManageErrorCode.ROLE_ONLY_CUSTOM_SCOPE_CAN_BIND_DEPTS, role.getRoleName());

        List<Long> normalizedIds = normalizeIds(deptIds);
        if (normalizedIds.isEmpty()) {
            roleDeptMapper.deleteByRoleId(roleId);
            return List.of();
        }

        List<DeptPO> depts = deptMapper.selectByIds(normalizedIds);
        AssertUtils.isTrue(depts.size() == normalizedIds.size(), AuthManageErrorCode.DEPT_IDS_INVALID);
        depts.forEach(dept -> AssertUtils.isTrue(EnableStatusEnum.isEnabled(dept.getStatus()), AuthManageErrorCode.DEPT_DISABLED_CANNOT_BIND, dept.getDeptName()));

        Set<Long> current = new LinkedHashSet<>(Optional.ofNullable(roleDeptMapper.selectDeptIdsByRoleId(roleId)).orElse(List.of()));
        Set<Long> target = new LinkedHashSet<>(normalizedIds);

        List<Long> toDelete = current.stream().filter(item -> !target.contains(item)).toList();
        if (!toDelete.isEmpty()) {
            roleDeptMapper.deleteByRoleIdAndDeptIds(roleId, toDelete);
        }
        for (Long deptId : target) {
            if (!current.contains(deptId)) {
                roleDeptMapper.insert(RoleDeptPO.builder().roleId(roleId).deptId(deptId).build());
            }
        }
        return target.stream()
                .map(item -> depts.stream().filter(dept -> Objects.equals(dept.getId(), item)).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .map(ISystemManageMapper.INSTANCE::toDTO)
                .toList();
    }

    /**
     * 根据标识查询目标数据，不存在时抛出异常。
     */
    private DeptPO getDeptOrThrow(Long id) {
        AssertUtils.notNull(id, AuthManageErrorCode.DEPT_ID_REQUIRED);
        DeptPO dept = deptMapper.selectById(id);
        AssertUtils.notNull(dept, AuthManageErrorCode.DEPT_NOT_FOUND);
        return dept;
    }

    /**
     * 处理内部辅助逻辑。
     */
    private DeptPO getParentOrNull(Long parentId) {
        if (parentId == null || parentId <= 0) {
            return null;
        }
        return getDeptOrThrow(parentId);
    }

    /**
     * 规范化父级部门标识。
     */
    private Long normalizeParentId(Long parentId) {
        return parentId == null ? 0L : parentId;
    }

    /**
     * 构建部门祖级路径。
     */
    private String buildAncestors(DeptPO parent) {
        if (parent == null) {
            return "0";
        }
        return "0".equals(parent.getAncestors()) ? "0," + parent.getId() : parent.getAncestors() + "," + parent.getId();
    }

    /**
     * 构建部门自身路径。
     */
    private String buildSelfPath(DeptPO dept) {
        return "0".equals(dept.getAncestors()) ? "0," + dept.getId() : dept.getAncestors() + "," + dept.getId();
    }

    /**
     * 判断当前条件是否成立。
     */
    private boolean isDescendantParent(Long currentId, DeptPO parent) {
        if (parent == null) {
            return false;
        }
        String ancestors = parent.getAncestors();
        String currentIdStr = String.valueOf(currentId);
        return currentId.equals(parent.getId())
                || ancestors.equals(currentIdStr)
                || ancestors.startsWith(currentIdStr + ",")
                || ancestors.contains("," + currentIdStr + ",")
                || ancestors.endsWith("," + currentIdStr);
    }

    /**
     * 校验名称是否唯一。
     */
    private void validateNameUnique(Long parentId, String deptName, Long excludeId) {
        AssertUtils.notBlank(deptName, AuthManageErrorCode.DEPT_NAME_REQUIRED);
        LambdaQueryWrapperX<DeptPO> wrapper = new LambdaQueryWrapperX<DeptPO>()
                .eq(DeptPO::getParentId, parentId)
                .eq(DeptPO::getDeptName, deptName);
        if (excludeId != null) {
            wrapper.ne(DeptPO::getId, excludeId);
        }
        AssertUtils.isTrue(deptMapper.selectCount(wrapper) == 0, AuthManageErrorCode.DEPT_NAME_ALREADY_EXISTS_UNDER_PARENT, deptName);
    }

    /**
     * 同步更新子节点的祖级路径。
     */
    private void updateChildrenAncestors(String oldPath, String newPath) {
        List<DeptPO> all = deptMapper.selectList();
        for (DeptPO item : all) {
            String ancestors = item.getAncestors();
            if (ancestors == null) {
                continue;
            }
            if (ancestors.equals(oldPath) || ancestors.startsWith(oldPath + ",")) {
                item.setAncestors(Pattern.compile("^" + Pattern.quote(oldPath)).matcher(ancestors).replaceFirst(newPath));
                DeptPO updatePO = new DeptPO();
                updatePO.setId(item.getId());
                updatePO.setAncestors(item.getAncestors());
                deptMapper.updateById(updatePO);
            }
        }
    }

    /**
     * 补齐层级结构中的上级节点。
     */
    private void appendAncestors(DeptDTO dept, Map<Long, DeptDTO> dtoMap, Map<Long, DeptDTO> collector) {
        if (dept.getParentId() == null || dept.getParentId() <= 0) {
            return;
        }
        DeptDTO parent = dtoMap.get(dept.getParentId());
        if (parent == null) {
            return;
        }
        collector.put(parent.getId(), parent);
        appendAncestors(parent, dtoMap, collector);
    }

    /**
     * 构建树形结构结果。
     */
    private Comparator<DeptDTO> deptTreeComparator() {
        return Comparator.comparing(DeptDTO::getSortNum, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(DeptDTO::getId, Comparator.nullsLast(Long::compareTo));
    }

    /**
     * 规范化标识集合并去重。
     */
    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null) {
            return List.of();
        }
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }
}
