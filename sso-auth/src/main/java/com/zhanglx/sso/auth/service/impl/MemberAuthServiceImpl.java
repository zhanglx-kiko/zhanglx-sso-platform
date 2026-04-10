package com.zhanglx.sso.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhanglx.sso.auth.config.Argon2PasswordEncoder;
import com.zhanglx.sso.auth.domain.dto.*;
import com.zhanglx.sso.auth.domain.po.MemberUserPO;
import com.zhanglx.sso.auth.domain.vo.LoginVO;
import com.zhanglx.sso.auth.enums.UserStatusEnum;
import com.zhanglx.sso.auth.exception.MemberErrorCode;
import com.zhanglx.sso.auth.exception.UserErrorCode;
import com.zhanglx.sso.auth.mapper.MemberUserMapper;
import com.zhanglx.sso.auth.service.MemberAuthService;
import com.zhanglx.sso.auth.service.MemberUserService;
import com.zhanglx.sso.auth.service.MemberVerificationCodeService;
import com.zhanglx.sso.auth.service.support.AuthLoginAuditSupport;
import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.core.utils.satoken.StpMemberUtil;
import com.zhanglx.sso.sms.enums.SmsSceneType;
import com.zhanglx.sso.web.support.RequestIdentityAccessor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberAuthServiceImpl implements MemberAuthService {

    private final MemberUserMapper memberUserMapper;
    private final MemberUserService memberUserService;
    private final Argon2PasswordEncoder argon2PasswordEncoder;
    private final MemberVerificationCodeService memberVerificationCodeService;
    private final AuthLoginAuditSupport authLoginAuditSupport;
    private final RequestIdentityAccessor requestIdentityAccessor;

    @Override
    public LoginVO login(MemberLoginDTO memberLoginDTO) {
        if (memberLoginDTO == null
                || !StringUtils.hasText(memberLoginDTO.getPhoneNumber())
                || !StringUtils.hasText(memberLoginDTO.getPassword())) {
            throw new BusinessException("member.account.empty");
        }

        MemberUserPO memberUserPO = memberUserService.findByPhoneNumber(memberLoginDTO.getPhoneNumber());
        if (memberUserPO == null) {
            throw new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND);
        }

        if (!StringUtils.hasText(memberUserPO.getPassword())
                || !argon2PasswordEncoder.matchesAsyncWithTimeout(memberLoginDTO.getPassword(), memberUserPO.getPassword())) {
            throw new BusinessException(UserErrorCode.USER_PASSWORD_ERROR);
        }

        if (UserStatusEnum.DISABLED.matches(memberUserPO.getStatus())) {
            throw new BusinessException(UserErrorCode.USER_ACCOUNT_DISABLED);
        }

        StpMemberUtil.login(memberUserPO.getId(), memberLoginDTO.getDevice());
        String displayName = resolveDisplayName(memberUserPO);
        authLoginAuditSupport.storeMemberSession(
                displayName,
                displayName,
                AuthLoginAuditSupport.CLIENT_TYPE_MEMBER_PASSWORD
        );

        if (argon2PasswordEncoder.needUpgrade(memberUserPO.getPassword())) {
            log.info("Password params need upgrade for member [{}]", memberUserPO.getId());
            upgradeUserPassword(memberUserPO, memberLoginDTO.getPassword());
        }

        memberUserService.touchLastLoginInfo(memberUserPO.getId());
        return assembleLoginVO(memberUserPO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(MemberRegisterDTO memberRegisterDTO) {
        MemberUserPO existMember = memberUserService.findByPhoneNumber(memberRegisterDTO.getPhoneNumber());
        if (existMember != null) {
            throw BusinessException.conflict("member.phone.already.bound");
        }

        memberVerificationCodeService.verifyCode(
                SmsSceneType.REGISTER,
                memberRegisterDTO.getPhoneNumber(),
                memberRegisterDTO.getCode(),
                null
        );

        MemberUserPO memberUserPO = buildDefaultMember();
        memberUserPO.setPhoneNumber(memberRegisterDTO.getPhoneNumber());
        memberUserPO.setPassword(argon2PasswordEncoder.encodeAsyncWithTimeout(memberRegisterDTO.getPassword()));
        memberUserPO.setNickname(maskPhoneNickname(memberRegisterDTO.getPhoneNumber()));
        memberUserMapper.insert(memberUserPO);

        StpMemberUtil.login(memberUserPO.getId(), memberRegisterDTO.getDevice());
        String displayName = resolveDisplayName(memberUserPO);
        authLoginAuditSupport.storeMemberSession(
                displayName,
                displayName,
                AuthLoginAuditSupport.CLIENT_TYPE_MEMBER_PASSWORD
        );
        memberUserService.touchLastLoginInfo(memberUserPO.getId());
        return assembleLoginVO(memberUserPO);
    }

    @Override
    public void sendVerificationCode(MemberVerificationCodeSendDTO sendDTO, Long memberId) {
        SmsSceneType sceneType = SmsSceneType.resolve(sendDTO.getScene())
                .orElseThrow(() -> new BusinessException("member.verification.scene.invalid"));

        switch (sceneType) {
            case REGISTER -> sendRegisterVerificationCode(sendDTO.getPhoneNumber());
            case FORGOT_PASSWORD -> sendForgotPasswordVerificationCode(sendDTO.getPhoneNumber());
            case BIND_PHONE -> sendBindPhoneVerificationCode(sendDTO.getPhoneNumber(), memberId);
            case CHANGE_BOUND_PHONE, VERIFY_BIND_PHONE ->
                    sendCurrentBoundPhoneVerificationCode(sceneType, sendDTO.getPhoneNumber(), memberId);
            default -> throw new BusinessException("member.verification.scene.invalid");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(UserPasswordDTO passwordDTO) {
        MemberUserPO memberUserPO = memberUserService.getById(passwordDTO.getUserId());
        if (!StringUtils.hasText(memberUserPO.getPassword())
                || !argon2PasswordEncoder.matchesAsyncWithTimeout(passwordDTO.getOldPassword(), memberUserPO.getPassword())) {
            throw new BusinessException(UserErrorCode.USER_OLD_PASSWORD_ERROR);
        }

        updateMemberPassword(memberUserPO.getId(), argon2PasswordEncoder.encodeAsyncWithTimeout(passwordDTO.getNewPassword()));
        StpMemberUtil.logout(memberUserPO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forgotPassword(MemberForgotPasswordDTO forgotPasswordDTO) {
        MemberUserPO memberUserPO = memberUserService.findByPhoneNumber(forgotPasswordDTO.getPhoneNumber());
        if (memberUserPO == null) {
            throw new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND);
        }

        memberVerificationCodeService.verifyCode(
                SmsSceneType.FORGOT_PASSWORD,
                forgotPasswordDTO.getPhoneNumber(),
                forgotPasswordDTO.getVerificationCode(),
                null
        );

        updateMemberPassword(memberUserPO.getId(), argon2PasswordEncoder.encodeAsyncWithTimeout(forgotPasswordDTO.getNewPassword()));
        StpMemberUtil.logout(memberUserPO.getId());
    }

    /**
     * 处理内部辅助逻辑。
     */
    private void sendRegisterVerificationCode(String phoneNumber) {
        if (memberUserService.findByPhoneNumber(phoneNumber) != null) {
            throw BusinessException.conflict("member.phone.already.bound");
        }

        memberVerificationCodeService.sendCode(SmsSceneType.REGISTER, phoneNumber, null);
    }

    /**
     * 处理内部辅助逻辑。
     */
    private void sendForgotPasswordVerificationCode(String phoneNumber) {
        if (memberUserService.findByPhoneNumber(phoneNumber) == null) {
            throw new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND);
        }
        memberVerificationCodeService.sendCode(SmsSceneType.FORGOT_PASSWORD, phoneNumber, null);
    }

    /**
     * 处理内部辅助逻辑。
     */
    private void sendBindPhoneVerificationCode(String phoneNumber, Long memberId) {
        MemberUserPO currentMember = requireLoginMember(memberId);
        if (phoneNumber.equals(currentMember.getPhoneNumber())) {
            throw BusinessException.badRequest("member.phone.bind.same.as.current");
        }

        MemberUserPO existMember = memberUserService.findByPhoneNumber(phoneNumber);
        if (existMember != null && !existMember.getId().equals(memberId)) {
            throw BusinessException.conflict("member.phone.already.bound");
        }

        memberVerificationCodeService.sendCode(SmsSceneType.BIND_PHONE, phoneNumber, memberId);
    }

    /**
     * 处理内部辅助逻辑。
     */
    private void sendCurrentBoundPhoneVerificationCode(SmsSceneType sceneType, String phoneNumber, Long memberId) {
        MemberUserPO currentMember = requireLoginMember(memberId);
        if (!StringUtils.hasText(currentMember.getPhoneNumber()) || !currentMember.getPhoneNumber().equals(phoneNumber)) {
            throw BusinessException.badRequest("member.phone.not.current.bound");
        }
        memberVerificationCodeService.sendCode(sceneType, phoneNumber, memberId);
    }

    /**
     * 校验必要条件并返回处理结果。
     */
    private MemberUserPO requireLoginMember(Long memberId) {
        if (memberId == null) {
            throw BusinessException.unauthorized("login.required");
        }
        return memberUserService.getById(memberId);
    }

    /**
     * 组装返回对象。
     */
    private LoginVO assembleLoginVO(MemberUserPO memberUserPO) {
        LoginVO loginVO = new LoginVO();
        loginVO.setId(memberUserPO.getId());
        String displayName = resolveDisplayName(memberUserPO);
        loginVO.setUsername(displayName);
        loginVO.setNickname(displayName);
        loginVO.setTokenName(StpMemberUtil.getStpLogic().getTokenName());
        loginVO.setTokenValue(StpMemberUtil.getTokenValue());
        return loginVO;
    }

    /**
     * 按新算法升级已有数据。
     */
    private void upgradeUserPassword(MemberUserPO memberUserPO, String rawPassword) {
        try {
            String newEncodedPassword = argon2PasswordEncoder.encodeAsyncWithTimeout(rawPassword);
            updateMemberPassword(memberUserPO.getId(), newEncodedPassword);
            log.info("Password upgraded for member [{}]", memberUserPO.getId());
        } catch (Exception e) {
            log.error("Failed to upgrade password for member [{}]", memberUserPO.getId(), e);
        }
    }

    /**
     * 更新会员密码。
     */
    private void updateMemberPassword(Long memberId, String encodedPassword) {
        memberUserMapper.update(
                null,
                new LambdaUpdateWrapper<MemberUserPO>()
                        .eq(MemberUserPO::getId, memberId)
                        .set(MemberUserPO::getPassword, encodedPassword)
        );
    }

    /**
     * 构建默认会员对象。
     */
    private MemberUserPO buildDefaultMember() {
        MemberUserPO memberUserPO = MemberUserPO.builder()
                .status(UserStatusEnum.NORMAL)
                .build();
        memberUserPO.setUserLevel(1);
        memberUserPO.setPoints(0L);
        memberUserPO.setMemberType(0);
        memberUserPO.setRealNameStatus(0);
        memberUserPO.setRegisterIp(resolveCurrentClientIp());
        return memberUserPO;
    }

    /**
     * 解析用于展示的名称。
     */
    private String resolveDisplayName(MemberUserPO memberUserPO) {
        if (StringUtils.hasText(memberUserPO.getNickname())) {
            return memberUserPO.getNickname();
        }
        if (StringUtils.hasText(memberUserPO.getPhoneNumber())) {
            return memberUserPO.getPhoneNumber();
        }
        return "member_" + memberUserPO.getId();
    }

    /**
     * 生成脱敏后的展示内容。
     */
    private String maskPhoneNickname(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber) || phoneNumber.length() < 7) {
            return null;
        }
        return "会员" + phoneNumber.substring(phoneNumber.length() - 4);
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
