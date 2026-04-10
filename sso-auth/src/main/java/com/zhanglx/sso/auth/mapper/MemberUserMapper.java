package com.zhanglx.sso.auth.mapper;

import com.zhanglx.sso.auth.domain.po.MemberUserPO;
import com.zhanglx.sso.mybatis.mapper.IBaseMapperX;
import org.apache.ibatis.annotations.Mapper;

/**
 * MemberUser数据访问层。
 */
@Mapper
public interface MemberUserMapper extends IBaseMapperX<MemberUserPO> {
}