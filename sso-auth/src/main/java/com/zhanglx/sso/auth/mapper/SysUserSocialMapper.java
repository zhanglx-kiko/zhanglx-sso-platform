package com.zhanglx.sso.auth.mapper;

import com.zhanglx.sso.auth.domain.po.SysUserSocialPO;
import com.zhanglx.sso.mybatis.mapper.IBaseMapperX;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysUserSocialMapper extends IBaseMapperX<SysUserSocialPO> {

    @Update("UPDATE t_sys_user_social SET del_flag = id WHERE user_id = #{userId} AND del_flag = 0")
    int deleteByUserId(@Param("userId") Long userId);
}
