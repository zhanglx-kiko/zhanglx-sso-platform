package com.zhanglx.sso.auth.service.impl;

import com.zhanglx.sso.auth.service.MemberVerificationCodeService;
import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.sms.enums.SmsSceneType;
import com.zhanglx.sso.sms.enums.SmsVerificationBusinessType;
import com.zhanglx.sso.sms.model.SmsVerificationCodeSendCommand;
import com.zhanglx.sso.sms.model.SmsVerificationCodeVerifyCommand;
import com.zhanglx.sso.sms.service.SmsVerificationCodeManager;
import com.zhanglx.sso.web.support.RequestIdentityAccessor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberVerificationCodeServiceImpl implements MemberVerificationCodeService {

    private final SmsVerificationCodeManager smsVerificationCodeManager;
    private final RequestIdentityAccessor requestIdentityAccessor;

    @Override
    public void sendCode(SmsSceneType sceneType, String phoneNumber, Long memberId) {
        SmsSceneType resolvedSceneType = requireScene(sceneType);
        String normalizedPhoneNumber = normalizePhoneNumber(phoneNumber);
        validateMemberScopedScene(resolvedSceneType, memberId);
        smsVerificationCodeManager.sendCode(
                SmsVerificationCodeSendCommand.builder()
                        .businessType(SmsVerificationBusinessType.MEMBER)
                        .sceneType(resolvedSceneType)
                        .phoneNumber(normalizedPhoneNumber)
                        .subjectKey(resolveSubjectKey(resolvedSceneType, memberId))
                        .clientIp(resolveCurrentClientIp())
                        .build()
        );
    }

    @Override
    public void verifyCode(SmsSceneType sceneType, String phoneNumber, String verificationCode, Long memberId) {
        SmsSceneType resolvedSceneType = requireScene(sceneType);
        String normalizedPhoneNumber = normalizePhoneNumber(phoneNumber);
        validateMemberScopedScene(resolvedSceneType, memberId);
        String normalizedVerificationCode = normalizeVerificationCode(verificationCode);
        smsVerificationCodeManager.verifyCode(
                SmsVerificationCodeVerifyCommand.builder()
                        .businessType(SmsVerificationBusinessType.MEMBER)
                        .sceneType(resolvedSceneType)
                        .phoneNumber(normalizedPhoneNumber)
                        .subjectKey(resolveSubjectKey(resolvedSceneType, memberId))
                        .verificationCode(normalizedVerificationCode)
                        .build()
        );
    }

    /**
     * 校验必要条件并返回处理结果。
     */
    private SmsSceneType requireScene(SmsSceneType sceneType) {
        if (sceneType == null) {
            throw new BusinessException("member.verification.scene.invalid");
        }

        return sceneType;
    }

    /**
     * 校验场景是否需要会员身份。
     */
    private void validateMemberScopedScene(SmsSceneType sceneType, Long memberId) {
        if (sceneType.isMemberScoped() && memberId == null) {
            throw BusinessException.unauthorized("login.required");
        }
    }

    /**
     * 规范化手机号参数。
     */
    private String normalizePhoneNumber(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            throw new BusinessException("member.phone.cannot.be.blank");
        }

        return phoneNumber.trim();
    }

    /**
     * 规范化验证码参数。
     */
    private String normalizeVerificationCode(String verificationCode) {
        if (!StringUtils.hasText(verificationCode)) {
            throw new BusinessException("member.verification.code.cannot.be.blank");
        }
        return verificationCode.trim();
    }

    /**
     * 解析当前请求的客户端 IP。
     */
    private String resolveCurrentClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes == null ? null : attributes.getRequest();
        return requestIdentityAccessor.resolveClientIp(request);
    }

    /**
     * 解析验证码场景对应的主体标识。
     */
    private String resolveSubjectKey(SmsSceneType sceneType, Long memberId) {
        if (!sceneType.isMemberScoped() || memberId == null) {
            return null;
        }
        return String.valueOf(memberId);
    }
}
