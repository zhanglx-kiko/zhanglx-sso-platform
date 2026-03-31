package com.zhanglx.sso.auth.utils;

import com.zhanglx.sso.auth.domain.dto.UserDTO;
import com.zhanglx.sso.auth.domain.po.UserPO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/10 20:49
 * @ClassName: IUserDomainMapper
 * @Description:
 */
// 禁用 MapStruct 的 Builder 功能，强制它使用无参构造函数（new）和 Setter 方法来完成映射。不然MapStruct 与 Lombok @SuperBuilder 配合使用时会产生冲突
@Mapper(builder = @Builder(disableBuilder = true))
public interface IUserDomainMapper {

    // MapStruct 的默认行为：当 MapStruct 发现你的目标对象（UserDTO）有 Builder 方法时，它会默认优先使用 Builder 模式（即 UserDTO.builder().xxx().build()）来构造对象，而不是使用普通的 new UserDTO() + setXxx()

    IUserDomainMapper INSTANCE = Mappers.getMapper(IUserDomainMapper.class);

    //    @Mappings({
//            @Mapping(target = "deptId", source = "deptId", qualifiedByName = "longToString"),
//            @Mapping(target = "id", source = "id", qualifiedByName = "longToString"),
//            @Mapping(target = "createBy", source = "createBy", qualifiedByName = "longToString"),
//            @Mapping(target = "updateBy", source = "updateBy", qualifiedByName = "longToString"),
//            @Mapping(target = "password", ignore = true)
//    })
//    })
//    @Mapping(target = "delFlag", ignore = true)
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
