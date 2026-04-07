package com.zhanglx.sso.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.AppDTO;
import com.zhanglx.sso.auth.domain.dto.AppQueryDTO;

import java.util.List;

public interface AppService {

    AppDTO create(AppDTO appDTO);

    AppDTO update(Long id, AppDTO appDTO);

    void delete(Long id);

    void batchDelete(List<Long> ids);

    AppDTO getById(Long id);

    Page<AppDTO> pageQuery(AppQueryDTO queryDTO);

    AppDTO updateStatus(Long id, Integer status);

    List<AppDTO> listByUser(Long userId);

    List<AppDTO> bindUserApps(Long userId, List<String> appCodes);
}
