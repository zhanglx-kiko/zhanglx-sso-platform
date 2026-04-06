package com.zhanglx.sso.auth.utils;

import com.zhanglx.sso.auth.domain.po.MemberUserPO;
import com.zhanglx.sso.auth.domain.vo.MemberInfoVO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(builder = @Builder(disableBuilder = true))
public interface IMemberUserMapper {

    IMemberUserMapper INSTANCE = Mappers.getMapper(IMemberUserMapper.class);

    @Mapping(target = "phoneBound",
            expression = "java(org.springframework.util.StringUtils.hasText(memberUserPO.getPhoneNumber()))")
    MemberInfoVO toInfoVO(MemberUserPO memberUserPO);
}
