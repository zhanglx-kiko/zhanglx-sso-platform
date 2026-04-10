package com.zhanglx.sso.auth.service.impl;

import com.zhanglx.sso.auth.domain.dto.ConfigDTO;
import com.zhanglx.sso.auth.domain.po.ConfigPO;
import com.zhanglx.sso.auth.enums.ConfigTypeEnum;
import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.auth.enums.YesNoEnum;
import com.zhanglx.sso.auth.exception.AuthManageErrorCode;
import com.zhanglx.sso.auth.mapper.ConfigMapper;
import com.zhanglx.sso.auth.service.runtime.ConfigValueMaskingSupport;
import com.zhanglx.sso.auth.service.runtime.DatabaseSystemConfigProvider;
import com.zhanglx.sso.core.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigServiceImplTest {

    @Mock
    private ConfigMapper configMapper;

    @Mock
    private DatabaseSystemConfigProvider systemConfigProvider;

    @Mock
    private ConfigValueMaskingSupport configValueMaskingSupport;

    private ConfigServiceImpl configService;

    @BeforeEach
    void setUp() {
        configService = new ConfigServiceImpl(configMapper, systemConfigProvider, configValueMaskingSupport);
    }

    @Test
    void shouldRejectCreateBuiltInConfigFromApi() {
        ConfigDTO dto = buildDto(ConfigTypeEnum.BUILT_IN);

        BusinessException exception = catchThrowableOfType(() -> configService.create(dto), BusinessException.class);
        assertThat(exception).isNotNull();
        assertThat(exception.getMessageKey()).isEqualTo(AuthManageErrorCode.CONFIG_BUILT_IN_CREATE_FORBIDDEN.getMessageKey());
        assertThat(exception.getCode()).isEqualTo(AuthManageErrorCode.CONFIG_BUILT_IN_CREATE_FORBIDDEN.getCode());

        verifyNoInteractions(configMapper, systemConfigProvider, configValueMaskingSupport);
    }

    @Test
    void shouldRejectUpdateBuiltInConfig() {
        when(configMapper.selectById(1L)).thenReturn(buildPo(1L, ConfigTypeEnum.BUILT_IN));

        BusinessException exception = catchThrowableOfType(() -> configService.update(1L, buildDto(ConfigTypeEnum.BUILT_IN)), BusinessException.class);
        assertThat(exception).isNotNull();
        assertThat(exception.getMessageKey()).isEqualTo(AuthManageErrorCode.CONFIG_BUILT_IN_UPDATE_FORBIDDEN.getMessageKey());
        assertThat(exception.getCode()).isEqualTo(AuthManageErrorCode.CONFIG_BUILT_IN_UPDATE_FORBIDDEN.getCode());

        verify(configMapper, never()).updateById(org.mockito.ArgumentMatchers.any(ConfigPO.class));
    }

    @Test
    void shouldRejectDeleteBuiltInConfig() {
        when(configMapper.selectById(1L)).thenReturn(buildPo(1L, ConfigTypeEnum.BUILT_IN));

        BusinessException exception = catchThrowableOfType(() -> configService.delete(1L), BusinessException.class);
        assertThat(exception).isNotNull();
        assertThat(exception.getMessageKey()).isEqualTo(AuthManageErrorCode.CONFIG_BUILT_IN_DELETE_FORBIDDEN.getMessageKey());
        assertThat(exception.getCode()).isEqualTo(AuthManageErrorCode.CONFIG_BUILT_IN_DELETE_FORBIDDEN.getCode());

        verify(configMapper, never()).deleteByIdWithFill(1L);
    }

    @Test
    void shouldRejectChangingConfigTypeForCustomConfig() {
        when(configMapper.selectById(1L)).thenReturn(buildPo(1L, ConfigTypeEnum.CUSTOM));

        BusinessException exception = catchThrowableOfType(() -> configService.update(1L, buildDto(ConfigTypeEnum.BUILT_IN)), BusinessException.class);
        assertThat(exception).isNotNull();
        assertThat(exception.getMessageKey()).isEqualTo(AuthManageErrorCode.CONFIG_TYPE_CANNOT_CHANGE.getMessageKey());
        assertThat(exception.getCode()).isEqualTo(AuthManageErrorCode.CONFIG_TYPE_CANNOT_CHANGE.getCode());

        verify(configMapper, never()).updateById(org.mockito.ArgumentMatchers.any(ConfigPO.class));
    }

    private ConfigDTO buildDto(ConfigTypeEnum configType) {
        ConfigDTO dto = new ConfigDTO();
        dto.setConfigName("测试参数");
        dto.setConfigKey("test.config");
        dto.setConfigValue("value");
        dto.setConfigGroup("default");
        dto.setSensitiveFlag(YesNoEnum.NO);
        dto.setStatus(EnableStatusEnum.ENABLED);
        dto.setConfigType(configType);
        dto.setRemark("remark");
        return dto;
    }

    private ConfigPO buildPo(Long id, ConfigTypeEnum configType) {
        ConfigPO po = new ConfigPO();
        po.setId(id);
        po.setConfigName("测试参数");
        po.setConfigKey("test.config");
        po.setConfigValue("value");
        po.setConfigGroup("default");
        po.setSensitiveFlag(YesNoEnum.NO);
        po.setStatus(EnableStatusEnum.ENABLED);
        po.setConfigType(configType);
        return po;
    }
}
