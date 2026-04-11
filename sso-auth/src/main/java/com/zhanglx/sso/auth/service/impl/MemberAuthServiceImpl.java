package com.zhanglx.sso.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhanglx.sso.auth.config.Argon2PasswordEncoder;
import com.zhanglx.sso.auth.domain.dto.MemberForgotPasswordDTO;
import com.zhanglx.sso.auth.domain.dto.MemberLoginDTO;
import com.zhanglx.sso.auth.domain.dto.MemberRegisterDTO;
import com.zhanglx.sso.auth.domain.dto.MemberVerificationCodeSendDTO;
import com.zhanglx.sso.auth.domain.dto.UserPasswordDTO;
import com.zhanglx.sso.auth.domain.po.MemberUserPO;
import com.zhanglx.sso.auth.domain.vo.LoginVO;
import com.zhanglx.sso.auth.enums.MemberTypeEnum;
import com.zhanglx.sso.auth.enums.RealNameStatusEnum;
import com.zhanglx.sso.auth.enums.UserStatusEnum;
import com.zhanglx.sso.auth.enums.YesNoEnum;
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

/**
 * MemberAuth 服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberAuthServiceImpl implements MemberAuthService {
    /**
     * 会员用户映射器。
     */
    private final MemberUserMapper memberUserMapper;
    /**
     * 会员用户服务。
     */
    private final MemberUserService memberUserService;
    /**
     * Argon2 密码编码器。
     */
    private final Argon2PasswordEncoder argon2PasswordEncoder;
    /**
     * 会员验证码服务。
     */
    private final MemberVerificationCodeService memberVerificationCodeService;
    /**
     * 登录审计支持组件。
     */
    private final AuthLoginAuditSupport authLoginAuditSupport;
    /**
     * 请求标识访问器。
     */
    private final RequestIdentityAccessor requestIdentityAccessor;

    @Override
    public LoginVO login(MemberLoginDTO memberLoginDTO) {
        if (memberLoginDTO == null
                || !StringUtils.hasText(memberLoginDTO.getPhoneNumber())
                || !StringUtils.hasText(memberLoginDTO.getPassword())) {
            throw new BusinessException(MemberErrorCode.MEMBER_ACCOUNT_EMPTY);
        }

        MemberUserPO memberUserPO = memberUserService.findByPhoneNumber(memberLoginDTO.getPhoneNumber());
        if (memberUserPO == null) {
            throw new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND);
        }

        if (!StringUtils.hasText(memberUserPO.getPassword())
                || !argon2PasswordEncoder.matchesAsyncWithTimeout(memberLoginDTO.getPassword(), memberUserPO.getPassword())) {
            throw new BusinessException(UserErrorCode.USER_PASSWORD_ERROR);
        }

        assertMemberCanLogin(memberUserPO);

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
            throw new BusinessException(MemberErrorCode.MEMBER_PHONE_ALREADY_BOUND);
        }

        memberVerificationCodeService.verifyCode(
                SmsSceneType.REGISTER,
                memberRegisterDTO.getPhoneNumber(),
                memberRegisterDTO.getCode(),
                null
        );

        MemberUserPO memberUserPO = buildDefaultMember("PHONE_REGISTER", memberRegisterDTO.getDevice(), YesNoEnum.YES);
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
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_VERIFICATION_SCENE_INVALID));

        switch (sceneType) {
            case REGISTER -> sendRegisterVerificationCode(sendDTO.getPhoneNumber());
            case FORGOT_PASSWORD -> sendForgotPasswordVerificationCode(sendDTO.getPhoneNumber());
            case BIND_PHONE -> sendBindPhoneVerificationCode(sendDTO.getPhoneNumber(), memberId);
            case CHANGE_BOUND_PHONE, VERIFY_BIND_PHONE ->
                    sendCurrentBoundPhoneVerificationCode(sceneType, sendDTO.getPhoneNumber(), memberId);
            default -> throw new BusinessException(MemberErrorCode.MEMBER_VERIFICATION_SCENE_INVALID);
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
            throw new BusinessException(MemberErrorCode.MEMBER_PHONE_ALREADY_BOUND);
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
            throw new BusinessException(MemberErrorCode.MEMBER_PHONE_BIND_SAME_AS_CURRENT);
        }

        MemberUserPO existMember = memberUserService.findByPhoneNumber(phoneNumber);
        if (existMember != null && !existMember.getId().equals(memberId)) {
            throw new BusinessException(MemberErrorCode.MEMBER_PHONE_ALREADY_BOUND);
        }

        memberVerificationCodeService.sendCode(SmsSceneType.BIND_PHONE, phoneNumber, memberId);
    }

    /**
     * 处理内部辅助逻辑。
     */
    private void sendCurrentBoundPhoneVerificationCode(SmsSceneType sceneType, String phoneNumber, Long memberId) {
        MemberUserPO currentMember = requireLoginMember(memberId);
        if (!StringUtils.hasText(currentMember.getPhoneNumber()) || !currentMember.getPhoneNumber().equals(phoneNumber)) {
            throw new BusinessException(MemberErrorCode.MEMBER_PHONE_NOT_CURRENT_BOUND);
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
     * 统一校验会员状态是否允许登录。
     */
    private void assertMemberCanLogin(MemberUserPO memberUserPO) {
        UserStatusEnum status = UserStatusEnum.normalize(memberUserPO.getStatus());
        if (status.isNormal()) {
            return;
        }
        if (status.isDisabled()) {
            throw new BusinessException(MemberErrorCode.MEMBER_ACCOUNT_DISABLED);
        }
        if (status.isFrozen()) {
            throw new BusinessException(MemberErrorCode.MEMBER_ACCOUNT_FROZEN);
        }
        throw new BusinessException(MemberErrorCode.MEMBER_ACCOUNT_CANCELLED);
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
    private MemberUserPO buildDefaultMember(String registerSource, String registerDevice, YesNoEnum phoneBound) {
        MemberUserPO memberUserPO = MemberUserPO.builder()
                .status(UserStatusEnum.NORMAL)
                .phoneBound(phoneBound)
                .memberType(MemberTypeEnum.NORMAL)
                .realNameStatus(RealNameStatusEnum.UNVERIFIED)
                .registerSource(registerSource)
                .registerDevice(StringUtils.hasText(registerDevice) ? registerDevice.trim() : "UNKNOWN")
                .riskLevel(0)
                .blacklistFlag(YesNoEnum.NO)
                .registerIp(resolveCurrentClientIp())
                .build();
        memberUserPO.setUserLevel(1);
        memberUserPO.setPoints(0L);
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