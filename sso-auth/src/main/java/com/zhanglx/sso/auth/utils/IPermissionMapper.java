package com.zhanglx.sso.auth.utils;

import com.zhanglx.sso.auth.domain.dto.PermissionDTO;
import com.zhanglx.sso.auth.domain.po.PermissionPO;
import com.zhanglx.sso.auth.domain.vo.PermissionExcelVO;
import com.zhanglx.sso.auth.domain.vo.PermissionVO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/10 20:49
 * @ClassName: IPermissionMapper
 * @Description:
 */
@Mapper(builder = @Builder(disableBuilder = true))
public interface IPermissionMapper {


    IPermissionMapper INSTANCE = Mappers.getMapper(IPermissionMapper.class);

    //    @Mapping(target = "delFlag", ignore = true)
    PermissionDTO toDTO(PermissionPO permissionPO);

    //    @Mapping(target = "children", ignore = true)
    PermissionPO toPO(PermissionDTO permissionDTO);

    List<PermissionDTO> toDTOList(List<PermissionPO> permissionPOList);

    List<PermissionPO> toPOList(List<PermissionDTO> permissionDTOList);

    List<PermissionVO> toVOList(List<PermissionPO> result);

    //    @Mapping(target = "delFlag", ignore = true)
    PermissionVO toVO(PermissionPO result);

    //    @Mapping(target = "errorMessage", ignore = true)
    PermissionPO excelVOToPo(PermissionExcelVO vo);

    //    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "createBy", ignore = true)
//    @Mapping(target = "createTime", ignore = true)
//    @Mapping(target = "updateBy", ignore = true)
//    @Mapping(target = "updateTime", ignore = true)
//    @Mapping(target = "delFlag", ignore = true)
    PermissionExcelVO poToExcelVo(PermissionPO po);
}
