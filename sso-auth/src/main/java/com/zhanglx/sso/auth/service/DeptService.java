package com.zhanglx.sso.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.DeptDTO;
import com.zhanglx.sso.auth.domain.dto.DeptQueryDTO;
import com.zhanglx.sso.auth.enums.EnableStatusEnum;

import java.util.List;

/**
 * 部门服务接口。
 */
public interface DeptService {

    DeptDTO create(DeptDTO deptDTO);

    DeptDTO update(Long id, DeptDTO deptDTO);

    void delete(Long id);

    DeptDTO getById(Long id);

    Page<DeptDTO> pageQuery(DeptQueryDTO queryDTO);

    List<DeptDTO> treeQuery(String deptName, Integer status);

    DeptDTO updateStatus(Long id, EnableStatusEnum status);

    List<DeptDTO> listByRole(Long roleId);

    List<DeptDTO> bindRoleDepts(Long roleId, List<Long> deptIds);
}