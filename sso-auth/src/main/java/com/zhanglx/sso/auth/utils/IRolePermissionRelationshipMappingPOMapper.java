package com.zhanglx.sso.auth.utils;

import com.zhanglx.sso.auth.domain.dto.RolePermissionRelationshipMappingDTO;
import com.zhanglx.sso.auth.domain.po.RolePermissionRelationshipMappingPO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/10 20:49
 * @ClassName: IRolePermissionRelationshipMappingPOMapper
 * @Description:
 */
@Mapper(builder = @Builder(disableBuilder = true))
public interface IRolePermissionRelationshipMappingPOMapper {

    IRolePermissionRelationshipMappingPOMapper INSTANCE = Mappers.getMapper(IRolePermissionRelationshipMappingPOMapper.class);

//    @Mapping(target = "delFlag", ignore = true)
    RolePermissionRelationshipMappingDTO toDTO(RolePermissionRelationshipMappingPO rolePermissionRelationshipMappingPO);

    RolePermissionRelationshipMappingPO toPO(RolePermissionRelationshipMappingDTO rolePermissionRelationshipMappingDTO);

    List<RolePermissionRelationshipMappingDTO> toDTOList(List<RolePermissionRelationshipMappingPO> rolePermissionRelationshipMappingPOList);

    List<RolePermissionRelationshipMappingPO> toPOList(List<RolePermissionRelationshipMappingDTO> rolePermissionRelationshipMappingDTOList);

}
