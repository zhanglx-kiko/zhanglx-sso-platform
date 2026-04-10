package com.zhanglx.sso.auth.mapper;

import com.zhanglx.sso.auth.domain.po.UserPO;
import com.zhanglx.sso.mybatis.mapper.IBaseMapperX;
import org.apache.ibatis.annotations.Mapper;

/**
 * 作者：Zhang L X
 * 创建时间：2026/2/10 20:46
 * 类名：UserMapper
 * 说明：
 */
@Mapper
public interface UserMapper extends IBaseMapperX<UserPO> {
}