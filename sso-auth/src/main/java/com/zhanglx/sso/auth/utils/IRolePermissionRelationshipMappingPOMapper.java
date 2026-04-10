package com.zhanglx.sso.auth.utils;

import com.zhanglx.sso.auth.domain.dto.RolePermissionRelationshipMappingDTO;
import com.zhanglx.sso.auth.domain.po.RolePermissionRelationshipMappingPO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 作者：Zhang L X
 * 创建时间：2026/2/10 20:49
 * 类名：角色PermissionRelationshipMappingPOMapper
 * 说明：
 */
@Mapper(builder = @Builder(disableBuilder = true))
public interface IRolePermissionRelationshipMappingPOMapper {

    IRolePermissionRelationshipMappingPOMapper INSTANCE = Mappers.getMapper(IRolePermissionRelationshipMappingPOMapper.class);

    RolePermissionRelationshipMappingDTO toDTO(RolePermissionRelationshipMappingPO rolePermissionRelationshipMappingPO);

    @Mapping(target = "delFlag", ignore = true)
    RolePermissionRelationshipMappingPO toPO(RolePermissionRelationshipMappingDTO rolePermissionRelationshipMappingDTO);

    List<RolePermissionRelationshipMappingDTO> toDTOList(List<RolePermissionRelationshipMappingPO> rolePermissionRelationshipMappingPOList);

    List<RolePermissionRelationshipMappingPO> toPOList(List<RolePermissionRelationshipMappingDTO> rolePermissionRelationshipMappingDTOList);

}