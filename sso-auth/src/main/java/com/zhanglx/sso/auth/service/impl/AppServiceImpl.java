package com.zhanglx.sso.auth.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.AppDTO;
import com.zhanglx.sso.auth.domain.dto.AppQueryDTO;
import com.zhanglx.sso.auth.domain.po.AppPO;
import com.zhanglx.sso.auth.domain.po.RolePO;
import com.zhanglx.sso.auth.domain.po.UserAppPO;
import com.zhanglx.sso.auth.domain.po.UserPO;
import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.auth.enums.UserTypeEnum;
import com.zhanglx.sso.auth.mapper.AppMapper;
import com.zhanglx.sso.auth.mapper.RoleMapper;
import com.zhanglx.sso.auth.mapper.UserAppMapper;
import com.zhanglx.sso.auth.mapper.UserMapper;
import com.zhanglx.sso.auth.service.AppService;
import com.zhanglx.sso.auth.utils.ISystemManageMapper;
import com.zhanglx.sso.core.exception.CommonErrorCode;
import com.zhanglx.sso.core.utils.AssertUtils;
import com.zhanglx.sso.mybatis.query.LambdaQueryWrapperX;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppServiceImpl implements AppService {

    private final AppMapper appMapper;
    private final RoleMapper roleMapper;
    private final UserMapper userMapper;
    private final UserAppMapper userAppMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppDTO create(AppDTO appDTO) {
        validateCodeUnique(appDTO.getAppCode(), null);
        validateNameUnique(appDTO.getAppName(), null);

        AppPO po = ISystemManageMapper.INSTANCE.toPO(appDTO);
        if (po.getStatus() == null) {
            po.setStatus(EnableStatusEnum.ENABLED.getCode());
        }
        if (po.getUserType() == null) {
            po.setUserType(UserTypeEnum.SYSTEM.getCode());
        }
        appMapper.insert(po);
        return ISystemManageMapper.INSTANCE.toDTO(po);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppDTO update(Long id, AppDTO appDTO) {
        AppPO exist = getAppOrThrow(id);
        validateCodeUnique(appDTO.getAppCode(), id);
        validateNameUnique(appDTO.getAppName(), id);

        exist.setAppCode(appDTO.getAppCode());
        exist.setAppName(appDTO.getAppName());
        exist.setUserType(appDTO.getUserType());
        exist.setStatus(appDTO.getStatus());
        exist.setRemark(appDTO.getRemark());
        appMapper.updateById(exist);
        return ISystemManageMapper.INSTANCE.toDTO(exist);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        AppPO exist = getAppOrThrow(id);
        AssertUtils.isTrue(userAppMapper.countByAppCode(exist.getAppCode()) == 0, "Current app is still assigned to users and cannot be deleted");
        AssertUtils.isTrue(roleMapper.selectCount(new LambdaQueryWrapperX<RolePO>()
                .eq(RolePO::getAppCode, exist.getAppCode())) == 0, "Current app still has roles and cannot be deleted");
        appMapper.deleteByIdWithFill(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        AssertUtils.notEmpty(ids, "app ids cannot be empty");
        ids.stream().filter(Objects::nonNull).distinct().forEach(this::delete);
    }

    @Override
    public AppDTO getById(Long id) {
        return ISystemManageMapper.INSTANCE.toDTO(getAppOrThrow(id));
    }

    @Override
    public Page<AppDTO> pageQuery(AppQueryDTO queryDTO) {
        Page<AppPO> page = Page.of(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapperX<AppPO> wrapper = new LambdaQueryWrapperX<AppPO>()
                .likeIfPresent(AppPO::getAppCode, queryDTO.getAppCode())
                .likeIfPresent(AppPO::getAppName, queryDTO.getAppName())
                .eqIfPresent(AppPO::getStatus, queryDTO.getStatus())
                .eqIfPresent(AppPO::getUserType, queryDTO.getUserType())
                .orderByDesc(AppPO::getCreateTime);

        if (StrUtil.isNotBlank(queryDTO.getSearchKey())) {
            wrapper.and(w -> w.like(AppPO::getAppCode, queryDTO.getSearchKey())
                    .or()
                    .like(AppPO::getAppName, queryDTO.getSearchKey()));
        }

        appMapper.selectPage(page, wrapper);
        return buildPage(page, ISystemManageMapper.INSTANCE.toAppDTOList(page.getRecords()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppDTO updateStatus(Long id, Integer status) {
        AppPO exist = getAppOrThrow(id);
        exist.setStatus(status);
        appMapper.updateById(exist);
        return ISystemManageMapper.INSTANCE.toDTO(exist);
    }

    @Override
    public List<AppDTO> listByUser(Long userId) {
        AssertUtils.notNull(userId, "user id cannot be null");
        List<String> appCodes = userAppMapper.selectAppCodesByUserId(userId);
        if (appCodes == null || appCodes.isEmpty()) {
            return List.of();
        }
        List<AppPO> apps = appMapper.selectList(new LambdaQueryWrapperX<AppPO>()
                .in(AppPO::getAppCode, appCodes)
                .orderByDesc(AppPO::getCreateTime));
        return ISystemManageMapper.INSTANCE.toAppDTOList(apps);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<AppDTO> bindUserApps(Long userId, List<String> appCodes) {
        UserPO user = userMapper.selectById(userId);
        AssertUtils.notNull(user, "user not found");

        List<String> normalizedCodes = normalizeAppCodes(appCodes);
        if (normalizedCodes.isEmpty()) {
            userAppMapper.deleteByUserId(userId);
            return List.of();
        }

        List<AppPO> apps = appMapper.selectList(new LambdaQueryWrapperX<AppPO>()
                .in(AppPO::getAppCode, normalizedCodes));
        AssertUtils.isTrue(apps.size() == normalizedCodes.size(), "invalid app code exists");

        apps.forEach(app -> {
            AssertUtils.isTrue(EnableStatusEnum.isEnabled(app.getStatus()), "disabled app cannot be assigned");
            AssertUtils.isTrue(UserTypeEnum.SYSTEM.matches(app.getUserType()), "only system-user apps support user assignment");
        });

        Set<String> target = new LinkedHashSet<>(normalizedCodes);
        Set<String> current = new LinkedHashSet<>(Optional.ofNullable(userAppMapper.selectAppCodesByUserId(userId)).orElse(List.of()));

        List<String> toDelete = current.stream().filter(code -> !target.contains(code)).toList();
        if (!toDelete.isEmpty()) {
            userAppMapper.deleteByUserIdAndAppCodes(userId, toDelete);
        }

        Map<String, AppPO> appMap = apps.stream().collect(Collectors.toMap(AppPO::getAppCode, Function.identity()));
        for (String code : target) {
            if (!current.contains(code)) {
                userAppMapper.insert(UserAppPO.builder().userId(userId).appCode(code).build());
            }
        }

        return target.stream()
                .map(appMap::get)
                .filter(Objects::nonNull)
                .map(ISystemManageMapper.INSTANCE::toDTO)
                .toList();
    }

    private AppPO getAppOrThrow(Long id) {
        AssertUtils.notNull(id, "app id cannot be null");
        AppPO exist = appMapper.selectById(id);
        AssertUtils.notNull(exist, CommonErrorCode.NOT_FOUND);
        return exist;
    }

    private void validateCodeUnique(String appCode, Long excludeId) {
        AssertUtils.notBlank(appCode, "app code cannot be blank");
        LambdaQueryWrapperX<AppPO> wrapper = new LambdaQueryWrapperX<AppPO>().eq(AppPO::getAppCode, appCode);
        if (excludeId != null) {
            wrapper.ne(AppPO::getId, excludeId);
        }
        AssertUtils.isTrue(appMapper.selectCount(wrapper) == 0, "app code already exists");
    }

    private void validateNameUnique(String appName, Long excludeId) {
        AssertUtils.notBlank(appName, "app name cannot be blank");
        LambdaQueryWrapperX<AppPO> wrapper = new LambdaQueryWrapperX<AppPO>().eq(AppPO::getAppName, appName);
        if (excludeId != null) {
            wrapper.ne(AppPO::getId, excludeId);
        }
        AssertUtils.isTrue(appMapper.selectCount(wrapper) == 0, "app name already exists");
    }

    private List<String> normalizeAppCodes(List<String> appCodes) {
        if (appCodes == null) {
            return List.of();
        }
        return appCodes.stream()
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .distinct()
                .toList();
    }

    private Page<AppDTO> buildPage(Page<AppPO> source, List<AppDTO> records) {
        Page<AppDTO> page = new Page<>();
        page.setCurrent(source.getCurrent());
        page.setSize(source.getSize());
        page.setTotal(source.getTotal());
        page.setRecords(records);
        return page;
    }
}