package com.zhanglx.sso.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhanglx.sso.auth.domain.dto.MemberBindPhoneDTO;
import com.zhanglx.sso.auth.domain.dto.MemberUpdateDTO;
import com.zhanglx.sso.auth.domain.po.MemberUserPO;
import com.zhanglx.sso.auth.domain.vo.MemberBasicVO;
import com.zhanglx.sso.auth.domain.vo.MemberInfoVO;
import com.zhanglx.sso.auth.enums.UserStatusEnum;
import com.zhanglx.sso.auth.enums.YesNoEnum;
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

import java.util.Collections;
import java.util.List;
import java.time.LocalDateTime;

/**
 * MemberUser 服务实现。
 */
@Service
@RequiredArgsConstructor
public class MemberUserServiceImpl implements MemberUserService {
    /**
     * 会员用户映射器。
     */
    private final MemberUserMapper memberUserMapper;
    /**
     * 会员社交账号映射器。
     */
    private final MemberSocialMapper memberSocialMapper;
    /**
     * 会员验证码服务。
     */
    private final MemberVerificationCodeService memberVerificationCodeService;
    /**
     * 请求标识访问器。
     */
    private final RequestIdentityAccessor requestIdentityAccessor;

    @Override
    public MemberInfoVO getCurrentMemberInfo(Long memberId) {
        return IMemberUserMapper.INSTANCE.toInfoVO(getById(memberId));
    }

    @Override
    public MemberBasicVO getCurrentMemberBasicInfo(Long memberId) {
        return toMemberBasicVO(getById(memberId));
    }

    @Override
    public List<MemberBasicVO> listMemberBasicInfo(List<Long> memberIds) {
        List<Long> normalizedIds = memberIds == null ? List.of() : memberIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        if (normalizedIds.isEmpty()) {
            return Collections.emptyList();
        }
        return memberUserMapper.selectList(new LambdaQueryWrapperX<MemberUserPO>()
                        .in(MemberUserPO::getId, normalizedIds))
                .stream()
                .map(this::toMemberBasicVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberInfoVO updateCurrentMember(Long memberId, MemberUpdateDTO updateDTO) {
        MemberUserPO memberUserPO = getById(memberId);
        if (StringUtils.hasText(updateDTO.getPhoneNumber())
                && !updateDTO.getPhoneNumber().equals(memberUserPO.getPhoneNumber())) {
            throw new BusinessException(MemberErrorCode.MEMBER_PHONE_UPDATE_REQUIRES_VERIFICATION);
        }

        LambdaUpdateWrapper<MemberUserPO> updateWrapper = new LambdaUpdateWrapper<MemberUserPO>()
                .eq(MemberUserPO::getId, memberId);
        boolean needUpdate = false;

        if (updateDTO.getNickname() != null) {
            memberUserPO.setNickname(updateDTO.getNickname());
            updateWrapper.set(MemberUserPO::getNickname, updateDTO.getNickname());
            needUpdate = true;
        }
        if (updateDTO.getAvatar() != null) {
            memberUserPO.setAvatar(updateDTO.getAvatar());
            updateWrapper.set(MemberUserPO::getAvatar, updateDTO.getAvatar());
            needUpdate = true;
        }
        if (updateDTO.getSex() != null) {
            memberUserPO.setSex(updateDTO.getSex());
            updateWrapper.set(MemberUserPO::getSex, updateDTO.getSex());
            needUpdate = true;
        }
        if (updateDTO.getBirthday() != null) {
            memberUserPO.setBirthday(updateDTO.getBirthday());
            updateWrapper.set(MemberUserPO::getBirthday, updateDTO.getBirthday());
            needUpdate = true;
        }
        if (updateDTO.getEmail() != null) {
            memberUserPO.setEmail(updateDTO.getEmail());
            updateWrapper.set(MemberUserPO::getEmail, updateDTO.getEmail());
            needUpdate = true;
        }
        if (updateDTO.getProfileExtra() != null) {
            memberUserPO.setProfileExtra(updateDTO.getProfileExtra());
            updateWrapper.set(MemberUserPO::getProfileExtra, updateDTO.getProfileExtra());
            needUpdate = true;
        }

        if (needUpdate) {
            memberUserMapper.update(null, updateWrapper);
        }
        return IMemberUserMapper.INSTANCE.toInfoVO(memberUserPO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MemberInfoVO bindPhone(Long memberId, MemberBindPhoneDTO bindPhoneDTO) {
        MemberUserPO memberUserPO = getById(memberId);
        if (bindPhoneDTO.getPhoneNumber().equals(memberUserPO.getPhoneNumber())) {
            throw new BusinessException(MemberErrorCode.MEMBER_PHONE_BIND_SAME_AS_CURRENT);
        }

        MemberUserPO existMember = findByPhoneNumber(bindPhoneDTO.getPhoneNumber());
        if (existMember != null && !existMember.getId().equals(memberId)) {
            throw new BusinessException(MemberErrorCode.MEMBER_PHONE_ALREADY_BOUND);
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
                        .set(MemberUserPO::getPhoneBound, YesNoEnum.YES)
        );
        memberUserPO.setPhoneNumber(bindPhoneDTO.getPhoneNumber());
        memberUserPO.setPhoneBound(YesNoEnum.YES);
        return IMemberUserMapper.INSTANCE.toInfoVO(memberUserPO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelCurrentMember(Long memberId) {
        MemberUserPO memberUserPO = getById(memberId);
        memberUserMapper.update(
                null,
                new LambdaUpdateWrapper<MemberUserPO>()
                        .eq(MemberUserPO::getId, memberUserPO.getId())
                        .set(MemberUserPO::getStatus, UserStatusEnum.CANCELLED)
                        .set(MemberUserPO::getStatusReason, "会员主动注销")
                        .set(MemberUserPO::getStatusExpireTime, null)
                        .set(MemberUserPO::getCancelTime, LocalDateTime.now())
        );
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
        AssertUtils.notBlank(phoneNumber, MemberErrorCode.MEMBER_PHONE_REQUIRED);
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

    private MemberBasicVO toMemberBasicVO(MemberUserPO memberUserPO) {
        return MemberBasicVO.builder()
                .id(memberUserPO.getId())
                .phoneNumber(memberUserPO.getPhoneNumber())
                .nickname(memberUserPO.getNickname())
                .avatar(memberUserPO.getAvatar())
                .phoneBound(YesNoEnum.YES.matches(memberUserPO.getPhoneBound()))
                .memberType(memberUserPO.getMemberType() == null ? null : memberUserPO.getMemberType().getCode())
                .realNameStatus(memberUserPO.getRealNameStatus() == null ? null : memberUserPO.getRealNameStatus().getCode())
                .status(memberUserPO.getStatus() == null ? null : memberUserPO.getStatus().getCode())
                .build();
    }
}
