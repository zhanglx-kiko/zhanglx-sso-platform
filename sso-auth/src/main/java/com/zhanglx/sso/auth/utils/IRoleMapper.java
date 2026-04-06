package com.zhanglx.sso.auth.utils;

import com.zhanglx.sso.auth.domain.dto.RoleDTO;
import com.zhanglx.sso.auth.domain.po.RolePO;
import com.zhanglx.sso.auth.domain.vo.RoleInfoVO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(builder = @Builder(disableBuilder = true))
public interface IRoleMapper {

    IRoleMapper INSTANCE = Mappers.getMapper(IRoleMapper.class);

    @Mapping(target = "rolePermissions", ignore = true)
    RoleDTO toDTO(RolePO rolePO);

    @Mapping(target = "delFlag", ignore = true)
    RolePO toPO(RoleDTO roleDTO);

    List<RoleDTO> toDTOList(List<RolePO> rolePOList);

    List<RolePO> toPOList(List<RoleDTO> roleDTOList);

    @Mapping(target = "userIds", ignore = true)
    RoleInfoVO toVO(RolePO roleResultPO);
}
