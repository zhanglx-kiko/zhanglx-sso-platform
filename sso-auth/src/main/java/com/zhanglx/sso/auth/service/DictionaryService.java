package com.zhanglx.sso.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.DictDataDTO;
import com.zhanglx.sso.auth.domain.dto.DictDataQueryDTO;
import com.zhanglx.sso.auth.domain.dto.DictTypeDTO;
import com.zhanglx.sso.auth.domain.dto.DictTypeQueryDTO;
import com.zhanglx.sso.auth.enums.EnableStatusEnum;

import java.util.List;

/**
 * 字典服务接口。
 */
public interface DictionaryService {

    DictTypeDTO createType(DictTypeDTO dto);

    DictTypeDTO updateType(Long id, DictTypeDTO dto);

    void deleteType(Long id);

    DictTypeDTO getType(Long id);

    Page<DictTypeDTO> pageType(DictTypeQueryDTO queryDTO);

    DictTypeDTO updateTypeStatus(Long id, EnableStatusEnum status);

    DictDataDTO createData(DictDataDTO dto);

    DictDataDTO updateData(Long id, DictDataDTO dto);

    void deleteData(Long id);

    DictDataDTO getData(Long id);

    Page<DictDataDTO> pageData(DictDataQueryDTO queryDTO);

    DictDataDTO updateDataStatus(Long id, EnableStatusEnum status);

    List<DictDataDTO> listDataByType(String dictType, EnableStatusEnum status);
}