package com.zhanglx.sso.auth.utils;

import com.zhanglx.sso.auth.domain.dto.UserDTO;
import com.zhanglx.sso.auth.domain.po.UserPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface IUserDomainMapper {

    IUserDomainMapper INSTANCE = Mappers.getMapper(IUserDomainMapper.class);

    //    @Mappings({
//            @Mapping(target = "deptId", source = "deptId", qualifiedByName = "longToString"),
//            @Mapping(target = "id", source = "id", qualifiedByName = "longToString"),
//            @Mapping(target = "createBy", source = "createBy", qualifiedByName = "longToString"),
//            @Mapping(target = "updateBy", source = "updateBy", qualifiedByName = "longToString"),
//            @Mapping(target = "password", ignore = true)
//    })
//    })
    @Mapping(target = "password", ignore = true)
    UserDTO toDTO(UserPO userPO);

    //    @Mappings({
//            @Mapping(target = "deptId", source = "deptId", qualifiedByName = "stringToLong"),
//            @Mapping(target = "id", source = "id", qualifiedByName = "stringToLong"),
//            @Mapping(target = "createBy", source = "createBy", qualifiedByName = "stringToLong"),
//            @Mapping(target = "updateBy", source = "updateBy", qualifiedByName = "stringToLong")
//    })
    UserPO toPO(UserDTO userDTO);

    List<UserDTO> toDTOList(List<UserPO> userPOList);

    List<UserPO> toPOList(List<UserDTO> userDTOList);

    /**
     * Long 转 String (用于 ID 字段，防止前端精度丢失)
     */
//    @Named("longToString")
//    default String longToString(Long value) {
//        return value == null ? null : String.valueOf(value);
//    }

    /**
     * String 转 Long (用于 ID 字段)
     */
//    @Named("stringToLong")
//    default Long stringToLong(String value) {
//        return value == null || value.isEmpty() ? null : Long.parseLong(value);
//    }

}
