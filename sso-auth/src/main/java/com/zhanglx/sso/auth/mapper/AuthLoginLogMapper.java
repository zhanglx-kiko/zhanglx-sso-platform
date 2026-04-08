package com.zhanglx.sso.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhanglx.sso.auth.domain.po.AuthLoginLogPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuthLoginLogMapper extends BaseMapper<AuthLoginLogPO> {
}
