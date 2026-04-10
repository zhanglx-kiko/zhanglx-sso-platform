package com.zhanglx.sso.auth.utils;

import com.zhanglx.sso.auth.domain.dto.UserDTO;
import com.zhanglx.sso.auth.domain.po.UserPO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * IUserDomain数据访问层。
 */
@Mapper(builder = @Builder(disableBuilder = true))
public interface IUserDomainMapper {

    IUserDomainMapper INSTANCE = Mappers.getMapper(IUserDomainMapper.class);

    @Mapping(target = "deptName", ignore = true)
    UserDTO toDTO(UserPO userPO);

    @Mapping(target = "delFlag", ignore = true)
    @Mapping(target = "userType", ignore = true)
    UserPO toPO(UserDTO userDTO);

    List<UserDTO> toDTOList(List<UserPO> userPOList);

    List<UserPO> toPOList(List<UserDTO> userDTOList);
}