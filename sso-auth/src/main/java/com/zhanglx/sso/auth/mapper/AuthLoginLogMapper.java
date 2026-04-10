package com.zhanglx.sso.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhanglx.sso.auth.domain.po.AuthLoginLogPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 认证登录日志数据访问层。
 */
@Mapper
public interface AuthLoginLogMapper extends BaseMapper<AuthLoginLogPO> {
}