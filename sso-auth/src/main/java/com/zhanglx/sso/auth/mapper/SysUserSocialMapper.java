package com.zhanglx.sso.auth.mapper;

import com.zhanglx.sso.auth.domain.po.SysUserSocialPO;
import com.zhanglx.sso.mybatis.mapper.IBaseMapperX;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserSocialMapper extends IBaseMapperX<SysUserSocialPO> {

    @Delete("DELETE FROM t_sys_user_social WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
