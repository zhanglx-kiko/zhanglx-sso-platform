package com.zhanglx.sso.auth.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.ConfigDTO;
import com.zhanglx.sso.auth.domain.dto.ConfigQueryDTO;
import com.zhanglx.sso.auth.domain.po.ConfigPO;
import com.zhanglx.sso.auth.enums.ConfigTypeEnum;
import com.zhanglx.sso.auth.mapper.ConfigMapper;
import com.zhanglx.sso.auth.service.ConfigService;
import com.zhanglx.sso.auth.utils.ISystemManageMapper;
import com.zhanglx.sso.core.exception.CommonErrorCode;
import com.zhanglx.sso.core.utils.AssertUtils;
import com.zhanglx.sso.mybatis.query.LambdaQueryWrapperX;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final ConfigMapper configMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfigDTO create(ConfigDTO dto) {
        validateKeyUnique(dto.getConfigKey(), null);
        ConfigPO po = ISystemManageMapper.INSTANCE.toPO(dto);
        if (po.getConfigType() == null) {
            po.setConfigType(ConfigTypeEnum.CUSTOM);
        }
        configMapper.insert(po);
        return ISystemManageMapper.INSTANCE.toDTO(po);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfigDTO update(Long id, ConfigDTO dto) {
        ConfigPO exist = getConfigOrThrow(id);
        validateKeyUnique(dto.getConfigKey(), id);

        if (ConfigTypeEnum.isBuiltIn(exist.getConfigType())) {
            AssertUtils.isTrue(StrUtil.equals(exist.getConfigKey(), dto.getConfigKey()), "built-in config key cannot be changed");
            AssertUtils.isTrue(ConfigTypeEnum.isBuiltIn(dto.getConfigType()), "built-in flag cannot be changed for built-in config");
        }

        exist.setConfigName(dto.getConfigName());
        exist.setConfigKey(dto.getConfigKey());
        exist.setConfigValue(dto.getConfigValue());
        exist.setConfigType(dto.getConfigType());
        exist.setRemark(dto.getRemark());
        configMapper.updateById(exist);
        return ISystemManageMapper.INSTANCE.toDTO(exist);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ConfigPO exist = getConfigOrThrow(id);
        AssertUtils.isTrue(!ConfigTypeEnum.isBuiltIn(exist.getConfigType()), "built-in config cannot be deleted");
        configMapper.deleteByIdWithFill(id);
    }

    @Override
    public ConfigDTO getById(Long id) {
        return ISystemManageMapper.INSTANCE.toDTO(getConfigOrThrow(id));
    }

    @Override
    public ConfigDTO getByKey(String configKey) {
        AssertUtils.notBlank(configKey, "config key cannot be blank");
        ConfigPO po = configMapper.selectOne(ConfigPO::getConfigKey, configKey);
        AssertUtils.notNull(po, CommonErrorCode.NOT_FOUND);
        return ISystemManageMapper.INSTANCE.toDTO(po);
    }

    @Override
    public Page<ConfigDTO> pageQuery(ConfigQueryDTO queryDTO) {
        Page<ConfigPO> page = Page.of(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapperX<ConfigPO> wrapper = new LambdaQueryWrapperX<ConfigPO>()
                .likeIfPresent(ConfigPO::getConfigName, queryDTO.getConfigName())
                .likeIfPresent(ConfigPO::getConfigKey, queryDTO.getConfigKey())
                .eqIfPresent(ConfigPO::getConfigType, queryDTO.getConfigType())
                .orderByDesc(ConfigPO::getCreateTime);

        if (StrUtil.isNotBlank(queryDTO.getSearchKey())) {
            wrapper.and(w -> w.like(ConfigPO::getConfigName, queryDTO.getSearchKey())
                    .or()
                    .like(ConfigPO::getConfigKey, queryDTO.getSearchKey()));
        }

        configMapper.selectPage(page, wrapper);
        Page<ConfigDTO> result = new Page<>();
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(ISystemManageMapper.INSTANCE.toConfigDTOList(page.getRecords()));
        return result;
    }

    private ConfigPO getConfigOrThrow(Long id) {
        AssertUtils.notNull(id, "config id cannot be null");
        ConfigPO exist = configMapper.selectById(id);
        AssertUtils.notNull(exist, CommonErrorCode.NOT_FOUND);
        return exist;
    }

    private void validateKeyUnique(String configKey, Long excludeId) {
        AssertUtils.notBlank(configKey, "config key cannot be blank");
        LambdaQueryWrapperX<ConfigPO> wrapper = new LambdaQueryWrapperX<ConfigPO>()
                .eq(ConfigPO::getConfigKey, configKey);
        if (excludeId != null) {
            wrapper.ne(ConfigPO::getId, excludeId);
        }
        AssertUtils.isTrue(configMapper.selectCount(wrapper) == 0, "config key already exists");
    }
}
