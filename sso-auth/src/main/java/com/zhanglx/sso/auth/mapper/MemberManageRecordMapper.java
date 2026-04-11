package com.zhanglx.sso.auth.mapper;

import com.zhanglx.sso.auth.domain.po.MemberManageRecordPO;
import com.zhanglx.sso.mybatis.mapper.IBaseMapperX;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员后台管理记录数据访问层。
 */
@Mapper
public interface MemberManageRecordMapper extends IBaseMapperX<MemberManageRecordPO> {
}