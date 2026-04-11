package com.zhanglx.sso.auth.utils;

import com.zhanglx.sso.auth.domain.po.MemberUserPO;
import com.zhanglx.sso.auth.domain.vo.MemberInfoVO;
import com.zhanglx.sso.auth.enums.YesNoEnum;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 会员对象映射器。
 */
@Mapper(builder = @Builder(disableBuilder = true), imports = YesNoEnum.class)
public interface IMemberUserMapper {

    IMemberUserMapper INSTANCE = Mappers.getMapper(IMemberUserMapper.class);

    @Mapping(target = "phoneBound",
            expression = "java(memberUserPO.getPhoneBound() != null ? YesNoEnum.YES.matches(memberUserPO.getPhoneBound()) : org.springframework.util.StringUtils.hasText(memberUserPO.getPhoneNumber()))")
    MemberInfoVO toInfoVO(MemberUserPO memberUserPO);
}