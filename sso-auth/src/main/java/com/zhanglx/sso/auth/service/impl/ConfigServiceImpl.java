package com.zhanglx.sso.auth.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.ConfigDTO;
import com.zhanglx.sso.auth.domain.dto.ConfigQueryDTO;
import com.zhanglx.sso.auth.domain.po.ConfigPO;
import com.zhanglx.sso.auth.enums.ConfigTypeEnum;
import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.auth.enums.YesNoEnum;
import com.zhanglx.sso.auth.mapper.ConfigMapper;
import com.zhanglx.sso.auth.service.ConfigService;
import com.zhanglx.sso.auth.service.runtime.ConfigValueMaskingSupport;
import com.zhanglx.sso.auth.service.runtime.DatabaseSystemConfigProvider;
import com.zhanglx.sso.auth.utils.ISystemManageMapper;
import com.zhanglx.sso.core.exception.CommonErrorCode;
import com.zhanglx.sso.core.utils.AssertUtils;
import com.zhanglx.sso.mybatis.query.LambdaQueryWrapperX;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 配置服务实现。
 */
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {
    /**
     * 参数配置映射器。
     */
    private final ConfigMapper configMapper;
    /**
     * 运行时配置提供者。
     */
    private final DatabaseSystemConfigProvider systemConfigProvider;
    /**
     * 参数脱敏支撑组件。
     */
    private final ConfigValueMaskingSupport configValueMaskingSupport;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfigDTO create(ConfigDTO dto) {
        AssertUtils.isTrue(!ConfigTypeEnum.isBuiltIn(dto.getConfigType()), "built-in config cannot be created from api");
        validateKeyUnique(dto.getConfigKey(), null);
        ConfigPO po = ISystemManageMapper.INSTANCE.toPO(dto);
        if (po.getConfigType() == null) {
            po.setConfigType(ConfigTypeEnum.CUSTOM);
        }
        if (StrUtil.isBlank(po.getConfigGroup())) {
            po.setConfigGroup("default");
        }
        if (po.getSensitiveFlag() == null) {
            po.setSensitiveFlag(YesNoEnum.NO);
        }
        if (po.getStatus() == null) {
            po.setStatus(EnableStatusEnum.ENABLED);
        }
        configMapper.insert(po);
        systemConfigProvider.refresh(po.getConfigKey());
        return toSafeDTO(po);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfigDTO update(Long id, ConfigDTO dto) {
        ConfigPO exist = getConfigOrThrow(id);
        AssertUtils.isTrue(!ConfigTypeEnum.isBuiltIn(exist.getConfigType()), "built-in config cannot be modified");
        AssertUtils.isTrue(exist.getConfigType() == dto.getConfigType(), "config type cannot be changed");
        String oldConfigKey = exist.getConfigKey();
        validateKeyUnique(dto.getConfigKey(), id);

        exist.setConfigName(dto.getConfigName());
        exist.setConfigKey(dto.getConfigKey());
        exist.setConfigValue(resolveConfigValueForUpdate(exist, dto.getConfigValue()));
        exist.setConfigGroup(dto.getConfigGroup());
        exist.setSensitiveFlag(dto.getSensitiveFlag());
        exist.setStatus(dto.getStatus());
        exist.setConfigType(dto.getConfigType());
        exist.setRemark(dto.getRemark());
        ConfigPO updatePO = new ConfigPO();
        updatePO.setId(id);
        updatePO.setConfigName(dto.getConfigName());
        updatePO.setConfigKey(dto.getConfigKey());
        updatePO.setConfigValue(exist.getConfigValue());
        updatePO.setConfigGroup(dto.getConfigGroup());
        updatePO.setSensitiveFlag(dto.getSensitiveFlag());
        updatePO.setStatus(dto.getStatus());
        updatePO.setConfigType(dto.getConfigType());
        updatePO.setRemark(dto.getRemark());
        configMapper.updateById(updatePO);
        systemConfigProvider.refresh(oldConfigKey);
        systemConfigProvider.refresh(dto.getConfigKey());
        return toSafeDTO(exist);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ConfigPO exist = getConfigOrThrow(id);
        AssertUtils.isTrue(!ConfigTypeEnum.isBuiltIn(exist.getConfigType()), "built-in config cannot be deleted");
        configMapper.deleteByIdWithFill(id);
        systemConfigProvider.refresh(exist.getConfigKey());
    }

    @Override
    public ConfigDTO getById(Long id) {
        return toSafeDTO(getConfigOrThrow(id));
    }

    @Override
    public ConfigDTO getByKey(String configKey) {
        AssertUtils.notBlank(configKey, "config key cannot be blank");
        ConfigPO po = configMapper.selectOne(ConfigPO::getConfigKey, configKey);
        AssertUtils.notNull(po, CommonErrorCode.NOT_FOUND);
        return toSafeDTO(po);
    }

    @Override
    public Page<ConfigDTO> pageQuery(ConfigQueryDTO queryDTO) {
        Page<ConfigPO> page = Page.of(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapperX<ConfigPO> wrapper = new LambdaQueryWrapperX<ConfigPO>()
                .likeIfPresent(ConfigPO::getConfigName, queryDTO.getConfigName())
                .likeIfPresent(ConfigPO::getConfigKey, queryDTO.getConfigKey())
                .eqIfPresent(ConfigPO::getConfigGroup, queryDTO.getConfigGroup())
                .eqIfPresent(ConfigPO::getSensitiveFlag, queryDTO.getSensitiveFlag())
                .eqIfPresent(ConfigPO::getStatus, queryDTO.getStatus())
                .eqIfPresent(ConfigPO::getConfigType, queryDTO.getConfigType())
                .orderByDesc(ConfigPO::getCreateTime);

        if (StrUtil.isNotBlank(queryDTO.getSearchKey())) {
            wrapper.and(w -> w.like(ConfigPO::getConfigName, queryDTO.getSearchKey())
                    .or()
                    .like(ConfigPO::getConfigKey, queryDTO.getSearchKey())
                    .or()
                    .like(ConfigPO::getConfigGroup, queryDTO.getSearchKey()));
        }

        configMapper.selectPage(page, wrapper);
        Page<ConfigDTO> result = new Page<>();
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toSafeDTO).toList());
        return result;
    }

    @Override
    public void refreshRuntimeCache() {
        systemConfigProvider.refreshAll();
    }

    /**
     * 根据标识查询目标数据，不存在时抛出异常。
     */
    private ConfigPO getConfigOrThrow(Long id) {
        AssertUtils.notNull(id, "config id cannot be null");
        ConfigPO exist = configMapper.selectById(id);
        AssertUtils.notNull(exist, CommonErrorCode.NOT_FOUND);
        return exist;
    }

    /**
     * 校验配置键是否唯一。
     */
    private void validateKeyUnique(String configKey, Long excludeId) {
        AssertUtils.notBlank(configKey, "config key cannot be blank");
        LambdaQueryWrapperX<ConfigPO> wrapper = new LambdaQueryWrapperX<ConfigPO>()
                .eq(ConfigPO::getConfigKey, configKey);
        if (excludeId != null) {
            wrapper.ne(ConfigPO::getId, excludeId);
        }
        AssertUtils.isTrue(configMapper.selectCount(wrapper) == 0, "config key already exists");
    }

    /**
     * 将参数转换为适合管理台展示的安全结果。
     * 敏感配置值统一返回脱敏占位符，避免明文下发到前端。
     */
    private ConfigDTO toSafeDTO(ConfigPO po) {
        ConfigDTO dto = ISystemManageMapper.INSTANCE.toDTO(po);
        dto.setConfigValue(configValueMaskingSupport.maskForDisplay(po));
        return dto;
    }

    /**
     * 处理敏感值编辑场景。
     * 当前端回传的是脱敏占位符时，表示继续沿用原值，不应把占位符写回数据库。
     */
    private String resolveConfigValueForUpdate(ConfigPO exist, String candidateValue) {
        if (configValueMaskingSupport.shouldKeepOriginalValue(exist, candidateValue)) {
            return exist.getConfigValue();
        }
        return candidateValue;
    }
}
