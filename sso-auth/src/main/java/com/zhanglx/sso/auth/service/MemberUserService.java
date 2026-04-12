package com.zhanglx.sso.auth.service;

import com.zhanglx.sso.auth.domain.dto.MemberBindPhoneDTO;
import com.zhanglx.sso.auth.domain.dto.MemberUpdateDTO;
import com.zhanglx.sso.auth.domain.po.MemberUserPO;
import com.zhanglx.sso.auth.domain.vo.MemberBasicVO;
import com.zhanglx.sso.auth.domain.vo.MemberInfoVO;

import java.util.List;

/**
 * MemberUser服务接口。
 */
public interface MemberUserService {

    MemberInfoVO getCurrentMemberInfo(Long memberId);

    MemberBasicVO getCurrentMemberBasicInfo(Long memberId);

    List<MemberBasicVO> listMemberBasicInfo(List<Long> memberIds);

    MemberInfoVO updateCurrentMember(Long memberId, MemberUpdateDTO updateDTO);

    MemberInfoVO bindPhone(Long memberId, MemberBindPhoneDTO bindPhoneDTO);

    void cancelCurrentMember(Long memberId);

    MemberUserPO getById(Long memberId);

    MemberUserPO getByPhoneNumber(String phoneNumber);

    MemberUserPO findByPhoneNumber(String phoneNumber);

    void touchLastLoginInfo(Long memberId);
}
