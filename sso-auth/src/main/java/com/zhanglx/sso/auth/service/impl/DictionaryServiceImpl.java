package com.zhanglx.sso.auth.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.dto.DictDataDTO;
import com.zhanglx.sso.auth.domain.dto.DictDataQueryDTO;
import com.zhanglx.sso.auth.domain.dto.DictTypeDTO;
import com.zhanglx.sso.auth.domain.dto.DictTypeQueryDTO;
import com.zhanglx.sso.auth.domain.po.DictDataPO;
import com.zhanglx.sso.auth.domain.po.DictTypePO;
import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.auth.exception.AuthManageErrorCode;
import com.zhanglx.sso.auth.mapper.DictDataMapper;
import com.zhanglx.sso.auth.mapper.DictTypeMapper;
import com.zhanglx.sso.auth.service.DictionaryService;
import com.zhanglx.sso.auth.service.support.AuthReferenceCheckSupport;
import com.zhanglx.sso.auth.utils.ISystemManageMapper;
import com.zhanglx.sso.core.utils.AssertUtils;
import com.zhanglx.sso.mybatis.query.LambdaQueryWrapperX;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 字典服务实现。
 */
@Service
@RequiredArgsConstructor
public class DictionaryServiceImpl implements DictionaryService {
    /**
     * 字典类型映射器。
     */
    private final DictTypeMapper dictTypeMapper;
    /**
     * 字典数据映射器。
     */
    private final DictDataMapper dictDataMapper;
    private final AuthReferenceCheckSupport authReferenceCheckSupport;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictTypeDTO createType(DictTypeDTO dto) {
        validateTypeUnique(dto.getDictType(), null);
        DictTypePO po = ISystemManageMapper.INSTANCE.toPO(dto);
        if (po.getStatus() == null) {
            po.setStatus(EnableStatusEnum.ENABLED);
        }
        dictTypeMapper.insert(po);
        return ISystemManageMapper.INSTANCE.toDTO(po);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictTypeDTO updateType(Long id, DictTypeDTO dto) {
        DictTypePO exist = getTypeOrThrow(id);
        validateTypeUnique(dto.getDictType(), id);

        String oldType = exist.getDictType();
        exist.setDictName(dto.getDictName());
        exist.setDictType(dto.getDictType());
        exist.setStatus(dto.getStatus());
        exist.setRemark(dto.getRemark());
        DictTypePO updatePO = new DictTypePO();
        updatePO.setId(id);
        updatePO.setDictName(dto.getDictName());
        updatePO.setDictType(dto.getDictType());
        updatePO.setStatus(dto.getStatus());
        updatePO.setRemark(dto.getRemark());
        dictTypeMapper.updateById(updatePO);

        if (!Objects.equals(oldType, dto.getDictType())) {
            List<DictDataPO> dataList = dictDataMapper.selectList(new LambdaQueryWrapperX<DictDataPO>()
                    .eq(DictDataPO::getDictType, oldType));
            for (DictDataPO data : dataList) {
                data.setDictType(dto.getDictType());
                DictDataPO updateDataPO = new DictDataPO();
                updateDataPO.setId(data.getId());
                updateDataPO.setDictType(dto.getDictType());
                dictDataMapper.updateById(updateDataPO);
            }
        }
        return ISystemManageMapper.INSTANCE.toDTO(exist);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteType(Long id) {
        DictTypePO exist = getTypeOrThrow(id);
        authReferenceCheckSupport.ensureDictTypeCanDelete(exist.getDictType());
        dictTypeMapper.deleteByIdWithFill(id);
    }

    @Override
    public DictTypeDTO getType(Long id) {
        return ISystemManageMapper.INSTANCE.toDTO(getTypeOrThrow(id));
    }

    @Override
    public Page<DictTypeDTO> pageType(DictTypeQueryDTO queryDTO) {
        Page<DictTypePO> page = Page.of(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapperX<DictTypePO> wrapper = new LambdaQueryWrapperX<DictTypePO>()
                .likeIfPresent(DictTypePO::getDictName, queryDTO.getDictName())
                .likeIfPresent(DictTypePO::getDictType, queryDTO.getDictType())
                .eqIfPresent(DictTypePO::getStatus, queryDTO.getStatus())
                .orderByDesc(DictTypePO::getCreateTime);

        if (StrUtil.isNotBlank(queryDTO.getSearchKey())) {
            wrapper.and(w -> w.like(DictTypePO::getDictName, queryDTO.getSearchKey())
                    .or()
                    .like(DictTypePO::getDictType, queryDTO.getSearchKey()));
        }

        dictTypeMapper.selectPage(page, wrapper);
        Page<DictTypeDTO> result = new Page<>();
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(ISystemManageMapper.INSTANCE.toDictTypeDTOList(page.getRecords()));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictTypeDTO updateTypeStatus(Long id, EnableStatusEnum status) {
        DictTypePO exist = getTypeOrThrow(id);
        exist.setStatus(status);
        DictTypePO updatePO = new DictTypePO();
        updatePO.setId(id);
        updatePO.setStatus(status);
        dictTypeMapper.updateById(updatePO);

        List<DictDataPO> dataList = dictDataMapper.selectList(new LambdaQueryWrapperX<DictDataPO>()
                .eq(DictDataPO::getDictType, exist.getDictType()));
        for (DictDataPO data : dataList) {
            data.setStatus(status);
            DictDataPO updateDataPO = new DictDataPO();
            updateDataPO.setId(data.getId());
            updateDataPO.setStatus(status);
            dictDataMapper.updateById(updateDataPO);
        }
        return ISystemManageMapper.INSTANCE.toDTO(exist);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictDataDTO createData(DictDataDTO dto) {
        DictTypePO type = getTypeByCode(dto.getDictType());
        AssertUtils.isTrue(EnableStatusEnum.isEnabled(type.getStatus()), AuthManageErrorCode.DICT_TYPE_DISABLED_CANNOT_CREATE_DATA, type.getDictName());
        validateDataUnique(dto.getDictType(), dto.getDictLabel(), dto.getDictValue(), null);

        DictDataPO po = ISystemManageMapper.INSTANCE.toPO(dto);
        if (po.getDictSort() == null) {
            po.setDictSort(0);
        }
        if (po.getStatus() == null) {
            po.setStatus(EnableStatusEnum.ENABLED);
        }
        dictDataMapper.insert(po);
        return ISystemManageMapper.INSTANCE.toDTO(po);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictDataDTO updateData(Long id, DictDataDTO dto) {
        DictDataPO exist = getDataOrThrow(id);
        getTypeByCode(dto.getDictType());
        validateDataUnique(dto.getDictType(), dto.getDictLabel(), dto.getDictValue(), id);

        exist.setDictSort(dto.getDictSort());
        exist.setDictLabel(dto.getDictLabel());
        exist.setDictValue(dto.getDictValue());
        exist.setDictType(dto.getDictType());
        exist.setStatus(dto.getStatus());
        exist.setRemark(dto.getRemark());
        DictDataPO updatePO = new DictDataPO();
        updatePO.setId(id);
        updatePO.setDictSort(dto.getDictSort());
        updatePO.setDictLabel(dto.getDictLabel());
        updatePO.setDictValue(dto.getDictValue());
        updatePO.setDictType(dto.getDictType());
        updatePO.setStatus(dto.getStatus());
        updatePO.setRemark(dto.getRemark());
        dictDataMapper.updateById(updatePO);
        return ISystemManageMapper.INSTANCE.toDTO(exist);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteData(Long id) {
        getDataOrThrow(id);
        dictDataMapper.deleteByIdWithFill(id);
    }

    @Override
    public DictDataDTO getData(Long id) {
        return ISystemManageMapper.INSTANCE.toDTO(getDataOrThrow(id));
    }

    @Override
    public Page<DictDataDTO> pageData(DictDataQueryDTO queryDTO) {
        Page<DictDataPO> page = Page.of(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapperX<DictDataPO> wrapper = new LambdaQueryWrapperX<DictDataPO>()
                .eqIfPresent(DictDataPO::getDictType, queryDTO.getDictType())
                .likeIfPresent(DictDataPO::getDictLabel, queryDTO.getDictLabel())
                .likeIfPresent(DictDataPO::getDictValue, queryDTO.getDictValue())
                .eqIfPresent(DictDataPO::getStatus, queryDTO.getStatus())
                .orderByAsc(DictDataPO::getDictSort)
                .orderByDesc(DictDataPO::getCreateTime);

        if (StrUtil.isNotBlank(queryDTO.getSearchKey())) {
            wrapper.and(w -> w.like(DictDataPO::getDictLabel, queryDTO.getSearchKey())
                    .or()
                    .like(DictDataPO::getDictValue, queryDTO.getSearchKey())
                    .or()
                    .like(DictDataPO::getDictType, queryDTO.getSearchKey()));
        }

        dictDataMapper.selectPage(page, wrapper);
        Page<DictDataDTO> result = new Page<>();
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(ISystemManageMapper.INSTANCE.toDictDataDTOList(page.getRecords()));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictDataDTO updateDataStatus(Long id, EnableStatusEnum status) {
        DictDataPO exist = getDataOrThrow(id);
        exist.setStatus(status);
        DictDataPO updatePO = new DictDataPO();
        updatePO.setId(id);
        updatePO.setStatus(status);
        dictDataMapper.updateById(updatePO);
        return ISystemManageMapper.INSTANCE.toDTO(exist);
    }

    @Override
    public List<DictDataDTO> listDataByType(String dictType, EnableStatusEnum status) {
        AssertUtils.notBlank(dictType, AuthManageErrorCode.DICT_TYPE_REQUIRED);
        List<DictDataPO> list = dictDataMapper.selectList(new LambdaQueryWrapperX<DictDataPO>()
                .eq(DictDataPO::getDictType, dictType)
                .eqIfPresent(DictDataPO::getStatus, status)
                .orderByAsc(DictDataPO::getDictSort));
        return ISystemManageMapper.INSTANCE.toDictDataDTOList(list);
    }

    /**
     * 根据标识查询目标数据，不存在时抛出异常。
     */
    private DictTypePO getTypeOrThrow(Long id) {
        AssertUtils.notNull(id, AuthManageErrorCode.DICT_TYPE_ID_REQUIRED);
        DictTypePO type = dictTypeMapper.selectById(id);
        AssertUtils.notNull(type, AuthManageErrorCode.DICT_TYPE_NOT_FOUND);
        return type;
    }

    /**
     * 根据编码查询字典类型。
     */
    private DictTypePO getTypeByCode(String dictType) {
        AssertUtils.notBlank(dictType, AuthManageErrorCode.DICT_TYPE_REQUIRED);
        DictTypePO type = dictTypeMapper.selectOne(DictTypePO::getDictType, dictType);
        AssertUtils.notNull(type, AuthManageErrorCode.DICT_TYPE_NOT_FOUND);
        return type;
    }

    /**
     * 根据标识查询目标数据，不存在时抛出异常。
     */
    private DictDataPO getDataOrThrow(Long id) {
        AssertUtils.notNull(id, AuthManageErrorCode.DICT_DATA_ID_REQUIRED);
        DictDataPO data = dictDataMapper.selectById(id);
        AssertUtils.notNull(data, AuthManageErrorCode.DICT_DATA_NOT_FOUND);
        return data;
    }

    /**
     * 校验字典类型是否唯一。
     */
    private void validateTypeUnique(String dictType, Long excludeId) {
        AssertUtils.notBlank(dictType, AuthManageErrorCode.DICT_TYPE_REQUIRED);
        LambdaQueryWrapperX<DictTypePO> wrapper = new LambdaQueryWrapperX<DictTypePO>()
                .eq(DictTypePO::getDictType, dictType);
        if (excludeId != null) {
            wrapper.ne(DictTypePO::getId, excludeId);
        }
        AssertUtils.isTrue(dictTypeMapper.selectCount(wrapper) == 0, AuthManageErrorCode.DICT_TYPE_ALREADY_EXISTS, dictType);
    }

    /**
     * 校验字典数据标签和值是否唯一。
     */
    private void validateDataUnique(String dictType, String dictLabel, String dictValue, Long excludeId) {
        AssertUtils.notBlank(dictLabel, AuthManageErrorCode.DICT_LABEL_REQUIRED);
        AssertUtils.notBlank(dictValue, AuthManageErrorCode.DICT_VALUE_REQUIRED);

        LambdaQueryWrapperX<DictDataPO> labelWrapper = new LambdaQueryWrapperX<DictDataPO>()
                .eq(DictDataPO::getDictType, dictType)
                .eq(DictDataPO::getDictLabel, dictLabel);
        LambdaQueryWrapperX<DictDataPO> valueWrapper = new LambdaQueryWrapperX<DictDataPO>()
                .eq(DictDataPO::getDictType, dictType)
                .eq(DictDataPO::getDictValue, dictValue);
        if (excludeId != null) {
            labelWrapper.ne(DictDataPO::getId, excludeId);
            valueWrapper.ne(DictDataPO::getId, excludeId);
        }
        AssertUtils.isTrue(dictDataMapper.selectCount(labelWrapper) == 0, AuthManageErrorCode.DICT_LABEL_ALREADY_EXISTS, dictType, dictLabel);
        AssertUtils.isTrue(dictDataMapper.selectCount(valueWrapper) == 0, AuthManageErrorCode.DICT_VALUE_ALREADY_EXISTS, dictType, dictValue);
    }
}
