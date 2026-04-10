package com.zhanglx.sso.auth.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.AppDTO;
import com.zhanglx.sso.auth.domain.dto.AppQueryDTO;
import com.zhanglx.sso.auth.domain.po.AppPO;
import com.zhanglx.sso.auth.domain.po.UserAppPO;
import com.zhanglx.sso.auth.domain.po.UserPO;
import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.auth.enums.UserTypeEnum;
import com.zhanglx.sso.auth.exception.AuthManageErrorCode;
import com.zhanglx.sso.auth.exception.UserErrorCode;
import com.zhanglx.sso.auth.mapper.AppMapper;
import com.zhanglx.sso.auth.mapper.UserAppMapper;
import com.zhanglx.sso.auth.mapper.UserMapper;
import com.zhanglx.sso.auth.service.AppService;
import com.zhanglx.sso.auth.service.support.AuthReferenceCheckSupport;
import com.zhanglx.sso.auth.utils.ISystemManageMapper;
import com.zhanglx.sso.core.utils.AssertUtils;
import com.zhanglx.sso.mybatis.query.LambdaQueryWrapperX;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 应用服务实现。
 */
@Service
@RequiredArgsConstructor
public class AppServiceImpl implements AppService {
    /**
     * 应用映射器。
     */
    private final AppMapper appMapper;
    /**
     * 用户映射器。
     */
    private final UserMapper userMapper;
    /**
     * 用户应用映射器。
     */
    private final UserAppMapper userAppMapper;
    private final AuthReferenceCheckSupport authReferenceCheckSupport;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppDTO create(AppDTO appDTO) {
        validateCodeUnique(appDTO.getAppCode(), null);
        validateNameUnique(appDTO.getAppName(), null);

        AppPO po = ISystemManageMapper.INSTANCE.toPO(appDTO);
        if (po.getStatus() == null) {
            po.setStatus(EnableStatusEnum.ENABLED);
        }
        if (po.getUserType() == null) {
            po.setUserType(UserTypeEnum.SYSTEM);
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
        AppPO updatePO = new AppPO();
        updatePO.setId(id);
        updatePO.setAppCode(appDTO.getAppCode());
        updatePO.setAppName(appDTO.getAppName());
        updatePO.setUserType(appDTO.getUserType());
        updatePO.setStatus(appDTO.getStatus());
        updatePO.setRemark(appDTO.getRemark());
        appMapper.updateById(updatePO);
        return ISystemManageMapper.INSTANCE.toDTO(exist);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        AppPO exist = getAppOrThrow(id);
        authReferenceCheckSupport.ensureAppCanDelete(exist.getAppCode(), exist.getAppName());
        appMapper.deleteByIdWithFill(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        AssertUtils.notEmpty(ids, AuthManageErrorCode.APP_IDS_EMPTY);
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
    public AppDTO updateStatus(Long id, EnableStatusEnum status) {
        AppPO exist = getAppOrThrow(id);
        exist.setStatus(status);
        AppPO updatePO = new AppPO();
        updatePO.setId(id);
        updatePO.setStatus(status);
        appMapper.updateById(updatePO);
        return ISystemManageMapper.INSTANCE.toDTO(exist);
    }

    @Override
    public List<AppDTO> listByUser(Long userId) {
        AssertUtils.notNull(userId, AuthManageErrorCode.USER_ID_REQUIRED);
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
        AssertUtils.notNull(user, UserErrorCode.USER_NOT_FOUND, userId);

        List<String> normalizedCodes = normalizeAppCodes(appCodes);
        if (normalizedCodes.isEmpty()) {
            userAppMapper.deleteByUserId(userId);
            return List.of();
        }

        List<AppPO> apps = appMapper.selectList(new LambdaQueryWrapperX<AppPO>()
                .in(AppPO::getAppCode, normalizedCodes));
        AssertUtils.isTrue(apps.size() == normalizedCodes.size(), AuthManageErrorCode.APP_CODE_INVALID);

        apps.forEach(app -> {
            AssertUtils.isTrue(EnableStatusEnum.isEnabled(app.getStatus()), AuthManageErrorCode.APP_DISABLED_CANNOT_ASSIGN, app.getAppName());
            AssertUtils.isTrue(UserTypeEnum.SYSTEM.matches(app.getUserType()), AuthManageErrorCode.APP_ONLY_SYSTEM_USER_ASSIGNMENT_SUPPORTED, app.getAppName());
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

    /**
     * 根据标识查询目标数据，不存在时抛出异常。
     */
    private AppPO getAppOrThrow(Long id) {
        AssertUtils.notNull(id, AuthManageErrorCode.APP_ID_REQUIRED);
        AppPO exist = appMapper.selectById(id);
        AssertUtils.notNull(exist, AuthManageErrorCode.APP_NOT_FOUND);
        return exist;
    }

    /**
     * 校验编码是否唯一。
     */
    private void validateCodeUnique(String appCode, Long excludeId) {
        AssertUtils.notBlank(appCode, AuthManageErrorCode.APP_CODE_REQUIRED);
        LambdaQueryWrapperX<AppPO> wrapper = new LambdaQueryWrapperX<AppPO>().eq(AppPO::getAppCode, appCode);
        if (excludeId != null) {
            wrapper.ne(AppPO::getId, excludeId);
        }
        AssertUtils.isTrue(appMapper.selectCount(wrapper) == 0, AuthManageErrorCode.APP_CODE_ALREADY_EXISTS, appCode);
    }

    /**
     * 校验名称是否唯一。
     */
    private void validateNameUnique(String appName, Long excludeId) {
        AssertUtils.notBlank(appName, AuthManageErrorCode.APP_NAME_REQUIRED);
        LambdaQueryWrapperX<AppPO> wrapper = new LambdaQueryWrapperX<AppPO>().eq(AppPO::getAppName, appName);
        if (excludeId != null) {
            wrapper.ne(AppPO::getId, excludeId);
        }
        AssertUtils.isTrue(appMapper.selectCount(wrapper) == 0, AuthManageErrorCode.APP_NAME_ALREADY_EXISTS, appName);
    }

    /**
     * 规范化输入参数。
     */
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

    /**
     * 构建分页返回结果。
     */
    private Page<AppDTO> buildPage(Page<AppPO> source, List<AppDTO> records) {
        Page<AppDTO> page = new Page<>();
        page.setCurrent(source.getCurrent());
        page.setSize(source.getSize());
        page.setTotal(source.getTotal());
        page.setRecords(records);
        return page;
    }
}
