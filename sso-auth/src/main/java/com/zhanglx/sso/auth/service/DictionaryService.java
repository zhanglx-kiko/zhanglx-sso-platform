package com.zhanglx.sso.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.*;

import java.util.List;

public interface DictionaryService {

    DictTypeDTO createType(DictTypeDTO dto);

    DictTypeDTO updateType(Long id, DictTypeDTO dto);

    void deleteType(Long id);

    DictTypeDTO getType(Long id);

    Page<DictTypeDTO> pageType(DictTypeQueryDTO queryDTO);

    DictTypeDTO updateTypeStatus(Long id, Integer status);

    DictDataDTO createData(DictDataDTO dto);

    DictDataDTO updateData(Long id, DictDataDTO dto);

    void deleteData(Long id);

    DictDataDTO getData(Long id);

    Page<DictDataDTO> pageData(DictDataQueryDTO queryDTO);

    DictDataDTO updateDataStatus(Long id, Integer status);

    List<DictDataDTO> listDataByType(String dictType, Integer status);
}
