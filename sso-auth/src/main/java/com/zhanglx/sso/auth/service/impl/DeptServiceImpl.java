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
import com.zhanglx.sso.auth.mapper.DeptMapper;
import com.zhanglx.sso.auth.mapper.RoleDeptMapper;
import com.zhanglx.sso.auth.mapper.RoleMapper;
import com.zhanglx.sso.auth.service.DeptService;
import com.zhanglx.sso.auth.utils.ISystemManageMapper;
import com.zhanglx.sso.core.exception.CommonErrorCode;
import com.zhanglx.sso.core.utils.AssertUtils;
import com.zhanglx.sso.mybatis.query.LambdaQueryWrapperX;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeptServiceImpl implements DeptService {

    private final DeptMapper deptMapper;
    private final RoleMapper roleMapper;
    private final RoleDeptMapper roleDeptMapper;

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
        AssertUtils.isTrue(!Objects.equals(id, parentId), "parent department cannot be self");

        DeptPO parent = getParentOrNull(parentId);
        String oldPath = buildSelfPath(exist);
        String newAncestors = buildAncestors(parent);
        AssertUtils.isFalse(isDescendantParent(id, parent), "parent department cannot be selected from current subtree");
        validateNameUnique(parentId, deptDTO.getDeptName(), id);

        exist.setParentId(parentId);
        exist.setAncestors(newAncestors);
        exist.setDeptName(deptDTO.getDeptName());
        exist.setSortNum(deptDTO.getSortNum());
        exist.setStatus(deptDTO.getStatus());
        deptMapper.updateById(exist);

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
        AssertUtils.isTrue(deptMapper.countChildren(id) == 0, "current department still has child departments");
        AssertUtils.isTrue(deptMapper.countUsers(id) == 0, "current department still has users");
        AssertUtils.isTrue(roleDeptMapper.countByDeptId(id) == 0, "current department is referenced by role data scope");
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
            return buildTree(allDtos);
        }

        Map<Long, DeptDTO> dtoMap = allDtos.stream().collect(Collectors.toMap(DeptDTO::getId, Function.identity()));
        LinkedHashMap<Long, DeptDTO> matched = new LinkedHashMap<>();
        for (DeptDTO dept : allDtos) {
            if (dept.getDeptName() != null && dept.getDeptName().contains(deptName)) {
                matched.put(dept.getId(), dept);
                appendAncestors(dept, dtoMap, matched);
            }
        }
        return buildTree(new ArrayList<>(matched.values()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeptDTO updateStatus(Long id, EnableStatusEnum status) {
        DeptPO exist = getDeptOrThrow(id);
        if (EnableStatusEnum.isEnabled(status) && exist.getParentId() != null && exist.getParentId() > 0) {
            DeptPO parent = getDeptOrThrow(exist.getParentId());
            AssertUtils.isTrue(EnableStatusEnum.isEnabled(parent.getStatus()), "parent department is disabled and current department cannot be enabled");
        }

        exist.setStatus(status);
        deptMapper.updateById(exist);

        if (EnableStatusEnum.isDisabled(status)) {
            String currentPath = buildSelfPath(exist);
            List<DeptPO> children = deptMapper.selectList().stream()
                    .filter(item -> item.getAncestors() != null
                            && (item.getAncestors().equals(currentPath) || item.getAncestors().startsWith(currentPath + ",")))
                    .toList();
            for (DeptPO child : children) {
                child.setStatus(EnableStatusEnum.DISABLED);
                deptMapper.updateById(child);
            }
        }
        return ISystemManageMapper.INSTANCE.toDTO(exist);
    }

    @Override
    public List<DeptDTO> listByRole(Long roleId) {
        AssertUtils.notNull(roleId, "role id cannot be null");
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
        AssertUtils.notNull(role, "role not found");
        AssertUtils.isTrue(DataScopeEnum.CUSTOM.matches(role.getDataScope()), "only custom data-scope roles can bind departments");

        List<Long> normalizedIds = normalizeIds(deptIds);
        if (normalizedIds.isEmpty()) {
            roleDeptMapper.deleteByRoleId(roleId);
            return List.of();
        }

        List<DeptPO> depts = deptMapper.selectByIds(normalizedIds);
        AssertUtils.isTrue(depts.size() == normalizedIds.size(), "invalid department id exists");
        depts.forEach(dept -> AssertUtils.isTrue(EnableStatusEnum.isEnabled(dept.getStatus()), "disabled department cannot be bound"));

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

    private DeptPO getDeptOrThrow(Long id) {
        AssertUtils.notNull(id, "department id cannot be null");
        DeptPO dept = deptMapper.selectById(id);
        AssertUtils.notNull(dept, CommonErrorCode.NOT_FOUND);
        return dept;
    }

    private DeptPO getParentOrNull(Long parentId) {
        if (parentId == null || parentId <= 0) {
            return null;
        }
        return getDeptOrThrow(parentId);
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null ? 0L : parentId;
    }

    private String buildAncestors(DeptPO parent) {
        if (parent == null) {
            return "0";
        }
        return "0".equals(parent.getAncestors()) ? "0," + parent.getId() : parent.getAncestors() + "," + parent.getId();
    }

    private String buildSelfPath(DeptPO dept) {
        return "0".equals(dept.getAncestors()) ? "0," + dept.getId() : dept.getAncestors() + "," + dept.getId();
    }

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

    private void validateNameUnique(Long parentId, String deptName, Long excludeId) {
        AssertUtils.notBlank(deptName, "department name cannot be blank");
        LambdaQueryWrapperX<DeptPO> wrapper = new LambdaQueryWrapperX<DeptPO>()
                .eq(DeptPO::getParentId, parentId)
                .eq(DeptPO::getDeptName, deptName);
        if (excludeId != null) {
            wrapper.ne(DeptPO::getId, excludeId);
        }
        AssertUtils.isTrue(deptMapper.selectCount(wrapper) == 0, "duplicate department name exists under same parent");
    }

    private void updateChildrenAncestors(String oldPath, String newPath) {
        List<DeptPO> all = deptMapper.selectList();
        for (DeptPO item : all) {
            String ancestors = item.getAncestors();
            if (ancestors == null) {
                continue;
            }
            if (ancestors.equals(oldPath) || ancestors.startsWith(oldPath + ",")) {
                item.setAncestors(Pattern.compile("^" + Pattern.quote(oldPath)).matcher(ancestors).replaceFirst(newPath));
                deptMapper.updateById(item);
            }
        }
    }

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

    private List<DeptDTO> buildTree(List<DeptDTO> depts) {
        Map<Long, DeptDTO> map = new LinkedHashMap<>();
        depts.stream()
                .sorted(Comparator.comparing(DeptDTO::getSortNum, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(DeptDTO::getId))
                .forEach(item -> {
                    item.setChildren(new ArrayList<>());
                    map.put(item.getId(), item);
                });

        List<DeptDTO> roots = new ArrayList<>();
        for (DeptDTO dept : map.values()) {
            Long parentId = dept.getParentId();
            if (parentId == null || parentId <= 0 || !map.containsKey(parentId)) {
                roots.add(dept);
                continue;
            }
            map.get(parentId).getChildren().add(dept);
        }
        return roots;
    }

    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null) {
            return List.of();
        }
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }
}
