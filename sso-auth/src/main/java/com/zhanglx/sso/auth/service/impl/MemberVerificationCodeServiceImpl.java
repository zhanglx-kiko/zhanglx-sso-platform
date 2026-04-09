package com.zhanglx.sso.auth.service.impl;

import com.zhanglx.sso.auth.constants.MemberVerificationCodeScenes;
import com.zhanglx.sso.auth.domain.properties.MemberVerificationCodeProperties;
import com.zhanglx.sso.auth.service.MemberVerificationCodeService;
import com.zhanglx.sso.auth.service.support.sms.SmsSender;
import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.web.support.RequestIdentityAccessor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberVerificationCodeServiceImpl implements MemberVerificationCodeService {

    private static final String CODE_CACHE_PREFIX = "sso:member:verification:code:";
    private static final String RESEND_CACHE_PREFIX = "sso:member:verification:resend:";
    private static final String PHONE_WINDOW_CACHE_PREFIX = "sso:member:verification:phone-window:";
    private static final String IP_DAILY_CACHE_PREFIX = "sso:member:verification:ip-daily:";

    private final StringRedisTemplate stringRedisTemplate;
    private final MemberVerificationCodeProperties properties;
    private final SmsSender smsSender;
    private final RequestIdentityAccessor requestIdentityAccessor;

    @Override
    public void sendCode(String scene, String phoneNumber, Long memberId) {
        doSendCode(scene, phoneNumber, memberId);
/*

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
*/
    }

    @Override
    public void verifyCode(String scene, String phoneNumber, String verificationCode, Long memberId) {
        doVerifyCode(scene, phoneNumber, verificationCode, memberId);
/*

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
*/
    }

    private String normalizeScene(String scene) {
        if (!StringUtils.hasText(scene)) {
            throw new BusinessException("member.verification.scene.cannot.be.blank");
        }

        String normalizedScene = scene.trim().toUpperCase(Locale.ROOT);
        if (MemberVerificationCodeScenes.isSupported(normalizedScene)) {
            return normalizedScene;
        }

        throw new BusinessException("member.verification.scene.invalid");
    }

    private String buildCacheKey(String prefix, String scene, String phoneNumber, Long memberId) {
        if (MemberVerificationCodeScenes.BIND_PHONE.equals(scene)) {
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

    private void doSendCode(String scene, String phoneNumber, Long memberId) {
        String normalizedScene = normalizeScene(scene);
        String normalizedPhoneNumber = normalizePhoneNumber(phoneNumber);
        String resendKey = buildCacheKey(RESEND_CACHE_PREFIX, normalizedScene, normalizedPhoneNumber, memberId);
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(resendKey))) {
            throw new BusinessException("member.verification.code.send.too.frequent");
        }

        ensurePhoneWindowLimit(normalizedPhoneNumber);
        String clientIp = resolveCurrentClientIp();
        ensureIpDailyLimit(clientIp);

        String verificationCode = generateVerificationCode();
        String smsText = buildSmsText(normalizedScene, verificationCode);
        String codeKey = buildCacheKey(CODE_CACHE_PREFIX, normalizedScene, normalizedPhoneNumber, memberId);
        String phoneWindowKey = buildPhoneWindowKey(normalizedPhoneNumber);
        String ipDailyKey = buildIpDailyKey(clientIp);
        reserveVerificationState(codeKey, resendKey, phoneWindowKey, ipDailyKey, verificationCode);

        try {
            if (properties.isMockSendEnabled()) {
                log.info("member verification code mocked, scene={}, phone={}, code={}",
                        normalizedScene,
                        maskPhone(normalizedPhoneNumber),
                        verificationCode);
                return;
            }

            smsSender.send(normalizedPhoneNumber, smsText);
            log.info("member verification code sent, scene={}, phone={}, clientIp={}",
                    normalizedScene,
                    maskPhone(normalizedPhoneNumber),
                    StringUtils.hasText(clientIp) ? clientIp : "unknown");
        } catch (RuntimeException e) {
            rollbackVerificationState(codeKey, resendKey, phoneWindowKey, ipDailyKey);
            throw e;
        }
    }

    private void doVerifyCode(String scene, String phoneNumber, String verificationCode, Long memberId) {
        String normalizedScene = normalizeScene(scene);
        String normalizedPhoneNumber = normalizePhoneNumber(phoneNumber);
        String codeKey = buildCacheKey(CODE_CACHE_PREFIX, normalizedScene, normalizedPhoneNumber, memberId);
        String storedCode = stringRedisTemplate.opsForValue().get(codeKey);
        if (!StringUtils.hasText(storedCode)) {
            throw new BusinessException("member.verification.code.expired");
        }

        if (!storedCode.equals(verificationCode)) {
            throw new BusinessException("invalid.verification.code");
        }

        stringRedisTemplate.delete(codeKey);
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            throw new BusinessException("member.phone.cannot.be.blank");
        }
        return phoneNumber.trim();
    }

    private String buildPhoneWindowKey(String phoneNumber) {
        return PHONE_WINDOW_CACHE_PREFIX + phoneNumber;
    }

    private String buildIpDailyKey(String clientIp) {
        if (!StringUtils.hasText(clientIp)) {
            return null;
        }

        LocalDate currentDay = LocalDate.now(ZoneId.systemDefault());
        return IP_DAILY_CACHE_PREFIX + currentDay + ":" + clientIp;
    }

    private void ensurePhoneWindowLimit(String phoneNumber) {
        if (properties.getPhoneWindowSeconds() <= 0 || properties.getPhoneWindowMaxSends() <= 0) {
            return;
        }

        Long currentCount = readCounter(buildPhoneWindowKey(phoneNumber));
        if (currentCount != null && currentCount >= properties.getPhoneWindowMaxSends()) {
            throw new BusinessException("member.verification.code.send.phone.limit.exceeded");
        }
    }

    private void ensureIpDailyLimit(String clientIp) {
        if (!StringUtils.hasText(clientIp) || properties.getIpDailyMaxSends() <= 0) {
            return;
        }

        Long currentCount = readCounter(buildIpDailyKey(clientIp));
        if (currentCount != null && currentCount >= properties.getIpDailyMaxSends()) {
            throw new BusinessException("member.verification.code.send.ip.limit.exceeded");
        }
    }

    private void reserveVerificationState(
            String codeKey,
            String resendKey,
            String phoneWindowKey,
            String ipDailyKey,
            String verificationCode
    ) {
        stringRedisTemplate.opsForValue().set(codeKey, verificationCode, properties.getExpireSeconds(), TimeUnit.SECONDS);
        stringRedisTemplate.opsForValue().set(resendKey, "1", properties.getResendIntervalSeconds(), TimeUnit.SECONDS);
        incrementCounter(phoneWindowKey, properties.getPhoneWindowSeconds());
        incrementCounter(ipDailyKey, resolveCurrentDayExpireSeconds());
    }

    private void rollbackVerificationState(
            String codeKey,
            String resendKey,
            String phoneWindowKey,
            String ipDailyKey
    ) {
        stringRedisTemplate.delete(codeKey);
        stringRedisTemplate.delete(resendKey);
        decrementCounter(phoneWindowKey);
        decrementCounter(ipDailyKey);
    }

    private Long readCounter(String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }

        String value = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.hasText(value)) {
            return null;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.warn("invalid member verification counter, key={}, value={}", key, value);
            stringRedisTemplate.delete(key);
            return null;
        }
    }

    private void incrementCounter(String key, long expireSeconds) {
        if (!StringUtils.hasText(key) || expireSeconds <= 0) {
            return;
        }

        Long currentCount = stringRedisTemplate.opsForValue().increment(key);
        if (currentCount == null) {
            return;
        }

        Long currentTtl = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (currentCount == 1L || currentTtl == null || currentTtl < 0) {
            stringRedisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS);
        }
    }

    private void decrementCounter(String key) {
        if (!StringUtils.hasText(key)) {
            return;
        }

        Long currentCount = stringRedisTemplate.opsForValue().increment(key, -1L);
        if (currentCount == null || currentCount <= 0L) {
            stringRedisTemplate.delete(key);
        }
    }

    private long resolveCurrentDayExpireSeconds() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime nextDay = now.toLocalDate().plusDays(1).atStartOfDay(now.getZone());
        return Math.max(1L, Duration.between(now, nextDay).getSeconds());
    }

    private String buildSmsText(String scene, String verificationCode) {
        String template = properties.getSceneTemplates().get(scene);
        if (!StringUtils.hasText(template)) {
            throw BusinessException.internalError("member.verification.sms.template.not.configured");
        }

        long expireMinutes = Math.max(1L, (properties.getExpireSeconds() + 59L) / 60L);
        try {
            return String.format(template, verificationCode, expireMinutes);
        } catch (IllegalFormatException e) {
            throw BusinessException.internalError("member.verification.sms.template.not.configured", e);
        }
    }

    private String resolveCurrentClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes == null ? null : attributes.getRequest();
        return requestIdentityAccessor.resolveClientIp(request);
    }

    private String maskPhone(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber) || phoneNumber.length() < 7) {
            return phoneNumber;
        }

        return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(phoneNumber.length() - 4);
    }
}
