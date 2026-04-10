package com.zhanglx.sso.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhanglx.sso.auth.domain.dto.MemberBindPhoneDTO;
import com.zhanglx.sso.auth.domain.dto.MemberUpdateDTO;
import com.zhanglx.sso.auth.domain.po.MemberUserPO;
import com.zhanglx.sso.auth.domain.vo.MemberInfoVO;
import com.zhanglx.sso.auth.exception.MemberErrorCode;
import com.zhanglx.sso.auth.mapper.MemberSocialMapper;
import com.zhanglx.sso.auth.mapper.MemberUserMapper;
import com.zhanglx.sso.auth.service.MemberUserService;
import com.zhanglx.sso.auth.service.MemberVerificationCodeService;
import com.zhanglx.sso.auth.utils.IMemberUserMapper;
import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.core.utils.AssertUtils;
import com.zhanglx.sso.core.utils.satoken.StpMemberUtil;
import com.zhanglx.sso.mybatis.query.LambdaQueryWrapperX;
import com.zhanglx.sso.sms.enums.SmsSceneType;
import com.zhanglx.sso.web.support.RequestIdentityAccessor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MemberUserServiceImpl implements MemberUserService {

    private final MemberUserMapper memberUserMapper;
    private final MemberSocialMapper memberSocialMapper;
    private final MemberVerificationCodeService memberVerificationCodeService;
    private final RequestIdentityAccessor requestIdentityAccessor;

    @Override
    public MemberInfoVO getCurrentMemberInfo(Long memberId) {
        return IMemberUserMapper.INSTANCE.toInfoVO(getById(memberId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberInfoVO updateCurrentMember(Long memberId, MemberUpdateDTO updateDTO) {
        MemberUserPO memberUserPO = getById(memberId);
        if (StringUtils.hasText(updateDTO.getPhoneNumber())
                && !updateDTO.getPhoneNumber().equals(memberUserPO.getPhoneNumber())) {
            throw BusinessException.badRequest("member.phone.update.requires.verification");
        }

        memberUserPO.setNickname(updateDTO.getNickname());
        memberUserPO.setAvatar(updateDTO.getAvatar());
        memberUserPO.setSex(updateDTO.getSex());
        memberUserPO.setBirthday(updateDTO.getBirthday());
        memberUserPO.setEmail(updateDTO.getEmail());
        memberUserPO.setProfileExtra(updateDTO.getProfileExtra());
        MemberUserPO updatePO = new MemberUserPO();
        updatePO.setId(memberId);
        updatePO.setNickname(updateDTO.getNickname());
        updatePO.setAvatar(updateDTO.getAvatar());
        updatePO.setSex(updateDTO.getSex());
        updatePO.setBirthday(updateDTO.getBirthday());
        updatePO.setEmail(updateDTO.getEmail());
        updatePO.setProfileExtra(updateDTO.getProfileExtra());
        memberUserMapper.updateById(updatePO);
        return IMemberUserMapper.INSTANCE.toInfoVO(memberUserPO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberInfoVO bindPhone(Long memberId, MemberBindPhoneDTO bindPhoneDTO) {
        MemberUserPO memberUserPO = getById(memberId);
        if (bindPhoneDTO.getPhoneNumber().equals(memberUserPO.getPhoneNumber())) {
            throw BusinessException.badRequest("member.phone.bind.same.as.current");
        }

        MemberUserPO existMember = findByPhoneNumber(bindPhoneDTO.getPhoneNumber());
        if (existMember != null && !existMember.getId().equals(memberId)) {
            throw BusinessException.conflict("member.phone.already.bound");
        }

        memberVerificationCodeService.verifyCode(
                SmsSceneType.BIND_PHONE,
                bindPhoneDTO.getPhoneNumber(),
                bindPhoneDTO.getVerificationCode(),
                memberId
        );

        memberUserMapper.update(
                null,
                new LambdaUpdateWrapper<MemberUserPO>()
                        .eq(MemberUserPO::getId, memberId)
                        .set(MemberUserPO::getPhoneNumber, bindPhoneDTO.getPhoneNumber())
        );
        memberUserPO.setPhoneNumber(bindPhoneDTO.getPhoneNumber());
        return IMemberUserMapper.INSTANCE.toInfoVO(memberUserPO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelCurrentMember(Long memberId) {
        MemberUserPO memberUserPO = getById(memberId);
        memberSocialMapper.deleteByMemberId(memberId);
        memberUserMapper.deleteByIdWithFill(memberUserPO.getId());
        StpMemberUtil.logout(memberId);
    }

    @Override
    public MemberUserPO getById(Long memberId) {
        AssertUtils.notNull(memberId, MemberErrorCode.MEMBER_NOT_FOUND);
        MemberUserPO memberUserPO = memberUserMapper.selectById(memberId);
        AssertUtils.notNull(memberUserPO, MemberErrorCode.MEMBER_NOT_FOUND);
        return memberUserPO;
    }

    @Override
    public MemberUserPO getByPhoneNumber(String phoneNumber) {
        AssertUtils.notBlank(phoneNumber, "member.phone.cannot.be.blank");
        MemberUserPO memberUserPO = findByPhoneNumber(phoneNumber);
        AssertUtils.notNull(memberUserPO, MemberErrorCode.MEMBER_NOT_FOUND);
        return memberUserPO;
    }

    @Override
    public MemberUserPO findByPhoneNumber(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            return null;
        }

        return memberUserMapper.selectOne(new LambdaQueryWrapperX<MemberUserPO>()
                .eq(MemberUserPO::getPhoneNumber, phoneNumber));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void touchLastLoginInfo(Long memberId) {
        memberUserMapper.update(
                null,
                new LambdaUpdateWrapper<MemberUserPO>()
                        .eq(MemberUserPO::getId, memberId)
                        .set(MemberUserPO::getLastLoginTime, LocalDateTime.now())
                        .set(MemberUserPO::getLastLoginIp, resolveCurrentClientIp())
        );
    }

    /**
     * 解析当前请求的客户端 IP。
     */
    private String resolveCurrentClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes == null ? null : attributes.getRequest();
        return requestIdentityAccessor.resolveClientIp(request);
    }
}
