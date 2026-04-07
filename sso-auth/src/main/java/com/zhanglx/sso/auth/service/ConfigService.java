package com.zhanglx.sso.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.ConfigDTO;
import com.zhanglx.sso.auth.domain.dto.ConfigQueryDTO;

public interface ConfigService {

    ConfigDTO create(ConfigDTO dto);

    ConfigDTO update(Long id, ConfigDTO dto);

    void delete(Long id);

    ConfigDTO getById(Long id);

    ConfigDTO getByKey(String configKey);

    Page<ConfigDTO> pageQuery(ConfigQueryDTO queryDTO);
}
