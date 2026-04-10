package com.zhanglx.sso.auth.mapper;

import com.zhanglx.sso.auth.domain.po.MemberSocialPO;
import com.zhanglx.sso.mybatis.mapper.IBaseMapperX;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberSocialMapper extends IBaseMapperX<MemberSocialPO> {

    /**
     * 根据会员ID逻辑删除第三方账号关联数据。
     */
    @Delete("DELETE FROM t_member_social WHERE member_id = #{memberId}")
    int deleteByMemberId(@Param("memberId") Long memberId);
}
