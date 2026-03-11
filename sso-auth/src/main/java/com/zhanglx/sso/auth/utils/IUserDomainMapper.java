package com.zhanglx.sso.auth.utils;

import com.zhanglx.sso.auth.domain.dto.UserDTO;
import com.zhanglx.sso.auth.domain.po.UserPO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface IUserDomainMapper {

    IUserDomainMapper INSTANCE = Mappers.getMapper(IUserDomainMapper.class);

    UserDTO toDTO(UserPO userPO);

    UserPO toPO(UserDTO userDTO);

    List<UserDTO> toDTOList(List<UserPO> userPOList);

    List<UserPO> toPOList(List<UserDTO> userDTOList);

}
