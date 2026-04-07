package com.zhanglx.sso.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.DeptDTO;
import com.zhanglx.sso.auth.domain.dto.DeptQueryDTO;

import java.util.List;

public interface DeptService {

    DeptDTO create(DeptDTO deptDTO);

    DeptDTO update(Long id, DeptDTO deptDTO);

    void delete(Long id);

    DeptDTO getById(Long id);

    Page<DeptDTO> pageQuery(DeptQueryDTO queryDTO);

    List<DeptDTO> treeQuery(String deptName, Integer status);

    DeptDTO updateStatus(Long id, Integer status);

    List<DeptDTO> listByRole(Long roleId);

    List<DeptDTO> bindRoleDepts(Long roleId, List<Long> deptIds);
}
