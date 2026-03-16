package com.zhanglx.sso.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhanglx.sso.auth.domain.po.UserPO;
import com.zhanglx.sso.mybatis.mapper.IBaseMapperX;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/10 20:46
 * @ClassName: UserMapper
 * @Description:
 */
@Mapper
public interface UserMapper extends IBaseMapperX<UserPO> {
}
