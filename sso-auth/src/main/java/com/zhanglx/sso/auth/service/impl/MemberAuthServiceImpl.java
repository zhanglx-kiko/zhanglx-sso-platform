package com.zhanglx.sso.auth.service.impl;

import com.zhanglx.sso.auth.config.Argon2PasswordEncoder;
import com.zhanglx.sso.auth.constants.MemberVerificationCodeScenes;
import com.zhanglx.sso.auth.domain.dto.MemberForgotPasswordDTO;
import com.zhanglx.sso.auth.domain.dto.MemberLoginDTO;
import com.zhanglx.sso.auth.domain.dto.MemberRegisterDTO;
import com.zhanglx.sso.auth.domain.dto.MemberVerificationCodeSendDTO;
import com.zhanglx.sso.auth.domain.dto.UserPasswordDTO;
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
import com.zhanglx.sso.web.support.RequestIdentityAccessor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Locale;

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
                MemberVerificationCodeScenes.REGISTER,
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
        String scene = sendDTO.getScene() == null ? null : sendDTO.getScene().trim().toUpperCase(Locale.ROOT);
        if (MemberVerificationCodeScenes.REGISTER.equals(scene)) {
            if (memberUserService.findByPhoneNumber(sendDTO.getPhoneNumber()) != null) {
                throw BusinessException.conflict("member.phone.already.bound");
            }
            memberVerificationCodeService.sendCode(scene, sendDTO.getPhoneNumber(), null);
            return;
        }

        if (MemberVerificationCodeScenes.FORGOT_PASSWORD.equals(scene)) {
            if (memberUserService.findByPhoneNumber(sendDTO.getPhoneNumber()) == null) {
                throw new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND);
            }
            memberVerificationCodeService.sendCode(scene, sendDTO.getPhoneNumber(), null);
            return;
        }

        if (MemberVerificationCodeScenes.BIND_PHONE.equals(scene)) {
            if (memberId == null) {
                throw BusinessException.unauthorized("login.required");
            }

            MemberUserPO currentMember = memberUserService.getById(memberId);
            if (sendDTO.getPhoneNumber().equals(currentMember.getPhoneNumber())) {
                throw BusinessException.badRequest("member.phone.bind.same.as.current");
            }

            MemberUserPO existMember = memberUserService.findByPhoneNumber(sendDTO.getPhoneNumber());
            if (existMember != null && !existMember.getId().equals(memberId)) {
                throw BusinessException.conflict("member.phone.already.bound");
            }

            memberVerificationCodeService.sendCode(scene, sendDTO.getPhoneNumber(), memberId);
            return;
        }

        throw new BusinessException("member.verification.scene.invalid");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(UserPasswordDTO passwordDTO) {
        MemberUserPO memberUserPO = memberUserService.getById(passwordDTO.getUserId());
        if (!StringUtils.hasText(memberUserPO.getPassword())
                || !argon2PasswordEncoder.matchesAsyncWithTimeout(passwordDTO.getOldPassword(), memberUserPO.getPassword())) {
            throw new BusinessException(UserErrorCode.USER_OLD_PASSWORD_ERROR);
        }

        memberUserPO.setPassword(argon2PasswordEncoder.encodeAsyncWithTimeout(passwordDTO.getNewPassword()));
        memberUserMapper.updateById(memberUserPO);
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
                MemberVerificationCodeScenes.FORGOT_PASSWORD,
                forgotPasswordDTO.getPhoneNumber(),
                forgotPasswordDTO.getVerificationCode(),
                null
        );

        memberUserPO.setPassword(argon2PasswordEncoder.encodeAsyncWithTimeout(forgotPasswordDTO.getNewPassword()));
        memberUserMapper.updateById(memberUserPO);
        StpMemberUtil.logout(memberUserPO.getId());
    }

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

    private void upgradeUserPassword(MemberUserPO memberUserPO, String rawPassword) {
        try {
            String newEncodedPassword = argon2PasswordEncoder.encodeAsyncWithTimeout(rawPassword);
            memberUserPO.setPassword(newEncodedPassword);
            memberUserMapper.updateById(memberUserPO);
            log.info("Password upgraded for member [{}]", memberUserPO.getId());
        } catch (Exception e) {
            log.error("Failed to upgrade password for member [{}]", memberUserPO.getId(), e);
        }
    }

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

    private String resolveDisplayName(MemberUserPO memberUserPO) {
        if (StringUtils.hasText(memberUserPO.getNickname())) {
            return memberUserPO.getNickname();
        }
        if (StringUtils.hasText(memberUserPO.getPhoneNumber())) {
            return memberUserPO.getPhoneNumber();
        }
        return "member_" + memberUserPO.getId();
    }

    private String maskPhoneNickname(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber) || phoneNumber.length() < 7) {
            return null;
        }
        return "会员" + phoneNumber.substring(phoneNumber.length() - 4);
    }

    private String resolveCurrentClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes == null ? null : attributes.getRequest();
        return requestIdentityAccessor.resolveClientIp(request);
    }
}
