package com.zhanglx.sso.auth.service.impl;

import com.zhanglx.sso.auth.domain.properties.MemberVerificationCodeProperties;
import com.zhanglx.sso.auth.service.MemberVerificationCodeService;
import com.zhanglx.sso.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberVerificationCodeServiceImpl implements MemberVerificationCodeService {

    public static final String SCENE_REGISTER = "REGISTER";
    public static final String SCENE_FORGOT_PASSWORD = "FORGOT_PASSWORD";
    public static final String SCENE_BIND_PHONE = "BIND_PHONE";

    private static final String CODE_CACHE_PREFIX = "sso:member:verification:code:";
    private static final String RESEND_CACHE_PREFIX = "sso:member:verification:resend:";

    private final StringRedisTemplate stringRedisTemplate;
    private final MemberVerificationCodeProperties properties;

    @Override
    public void sendCode(String scene, String phoneNumber, Long memberId) {
        String normalizedScene = normalizeScene(scene);
        String resendKey = buildCacheKey(RESEND_CACHE_PREFIX, normalizedScene, phoneNumber, memberId);
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(resendKey))) {
            throw new BusinessException("member.verification.code.send.too.frequent");
        }

        String verificationCode = generateVerificationCode();
        String codeKey = buildCacheKey(CODE_CACHE_PREFIX, normalizedScene, phoneNumber, memberId);
        stringRedisTemplate.opsForValue().set(codeKey, verificationCode, properties.getExpireSeconds(), TimeUnit.SECONDS);
        stringRedisTemplate.opsForValue().set(
                resendKey,
                "1",
                properties.getResendIntervalSeconds(),
                TimeUnit.SECONDS
        );

        if (properties.isMockSendEnabled()) {
            log.info("会员验证码已生成，scene={}, phone={}, code={}", normalizedScene, maskPhone(phoneNumber), verificationCode);
            return;
        }

        log.warn("会员验证码已写入缓存，但当前尚未接入短信通道，scene={}, phone={}", normalizedScene, maskPhone(phoneNumber));
    }

    @Override
    public void verifyCode(String scene, String phoneNumber, String verificationCode, Long memberId) {
        String normalizedScene = normalizeScene(scene);
        String codeKey = buildCacheKey(CODE_CACHE_PREFIX, normalizedScene, phoneNumber, memberId);
        String storedCode = stringRedisTemplate.opsForValue().get(codeKey);
        if (!StringUtils.hasText(storedCode)) {
            throw new BusinessException("member.verification.code.expired");
        }

        if (!storedCode.equals(verificationCode)) {
            throw new BusinessException("invalid.verification.code");
        }

        stringRedisTemplate.delete(codeKey);
    }

    private String normalizeScene(String scene) {
        if (!StringUtils.hasText(scene)) {
            throw new BusinessException("member.verification.scene.cannot.be.blank");
        }

        String normalizedScene = scene.trim().toUpperCase(Locale.ROOT);
        if (SCENE_REGISTER.equals(normalizedScene)
                || SCENE_FORGOT_PASSWORD.equals(normalizedScene)
                || SCENE_BIND_PHONE.equals(normalizedScene)) {
            return normalizedScene;
        }

        throw new BusinessException("member.verification.scene.invalid");
    }

    private String buildCacheKey(String prefix, String scene, String phoneNumber, Long memberId) {
        if (SCENE_BIND_PHONE.equals(scene)) {
            return prefix + scene + ":" + memberId + ":" + phoneNumber;
        }
        return prefix + scene + ":" + phoneNumber;
    }

    private String generateVerificationCode() {
        int codeLength = properties.getCodeLength() < 4 ? 6 : properties.getCodeLength();
        int min = (int) Math.pow(10, codeLength - 1);
        int max = (int) Math.pow(10, codeLength);
        int code = ThreadLocalRandom.current().nextInt(min, max);
        return String.format("%0" + codeLength + "d", code);
    }

    private String maskPhone(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber) || phoneNumber.length() < 7) {
            return phoneNumber;
        }

        return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(phoneNumber.length() - 4);
    }
}
