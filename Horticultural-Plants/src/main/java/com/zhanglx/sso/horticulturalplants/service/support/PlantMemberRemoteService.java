package com.zhanglx.sso.horticulturalplants.service.support;

import com.zhanglx.sso.horticulturalplants.remote.auth.AuthMemberBasicVO;

import java.util.Collection;
import java.util.Map;

public interface PlantMemberRemoteService {

    AuthMemberBasicVO getCurrentMemberBasic();

    Map<Long, AuthMemberBasicVO> queryMemberBasicMap(Collection<Long> memberIds);
}
