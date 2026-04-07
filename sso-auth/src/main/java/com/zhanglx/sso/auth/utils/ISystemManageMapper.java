package com.zhanglx.sso.auth.utils;

import com.zhanglx.sso.auth.domain.dto.*;
import com.zhanglx.sso.auth.domain.po.*;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(builder = @Builder(disableBuilder = true))
public interface ISystemManageMapper {

    ISystemManageMapper INSTANCE = Mappers.getMapper(ISystemManageMapper.class);

    AppDTO toDTO(AppPO po);

    @Mapping(target = "delFlag", ignore = true)
    AppPO toPO(AppDTO dto);

    List<AppDTO> toAppDTOList(List<AppPO> list);

    @Mapping(target = "children", ignore = true)
    DeptDTO toDTO(DeptPO po);

    @Mapping(target = "delFlag", ignore = true)
    DeptPO toPO(DeptDTO dto);

    List<DeptDTO> toDeptDTOList(List<DeptPO> list);

    PostDTO toDTO(PostPO po);

    @Mapping(target = "delFlag", ignore = true)
    PostPO toPO(PostDTO dto);

    List<PostDTO> toPostDTOList(List<PostPO> list);

    DictTypeDTO toDTO(DictTypePO po);

    @Mapping(target = "delFlag", ignore = true)
    DictTypePO toPO(DictTypeDTO dto);

    List<DictTypeDTO> toDictTypeDTOList(List<DictTypePO> list);

    DictDataDTO toDTO(DictDataPO po);

    @Mapping(target = "delFlag", ignore = true)
    DictDataPO toPO(DictDataDTO dto);

    List<DictDataDTO> toDictDataDTOList(List<DictDataPO> list);

    ConfigDTO toDTO(ConfigPO po);

    @Mapping(target = "delFlag", ignore = true)
    ConfigPO toPO(ConfigDTO dto);

    List<ConfigDTO> toConfigDTOList(List<ConfigPO> list);
}
