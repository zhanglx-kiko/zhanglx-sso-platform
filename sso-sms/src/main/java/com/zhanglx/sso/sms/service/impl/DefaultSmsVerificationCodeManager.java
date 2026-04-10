package com.zhanglx.sso.sms.service.impl;

import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.sms.model.SmsSendRequest;
import com.zhanglx.sso.sms.model.SmsVerificationCodeSendCommand;
import com.zhanglx.sso.sms.model.SmsVerificationCodeSendResult;
import com.zhanglx.sso.sms.model.SmsVerificationCodeVerifyCommand;
import com.zhanglx.sso.sms.properties.SmsVerificationCodeProperties;
import com.zhanglx.sso.sms.service.SmsGateway;
import com.zhanglx.sso.sms.service.SmsVerificationCodeManager;
import com.zhanglx.sso.sms.support.SmsMaskingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 默认短信验证码管理器类型。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(StringRedisTemplate.class)
public class DefaultSmsVerificationCodeManager implements SmsVerificationCodeManager {

    private static final String CODE_CACHE_PREFIX = "sso:sms:verification:code:";
    private static final String RESEND_CACHE_PREFIX = "sso:sms:verification:resend:";
    private static final String PHONE_WINDOW_CACHE_PREFIX = "sso:sms:verification:phone-window:";
    private static final String IP_DAILY_CACHE_PREFIX = "sso:sms:verification:ip-daily:";
    private static final int FIXED_CODE_LENGTH = 6;
    private static final long FIXED_EXPIRE_SECONDS = 180L;
    private static final String FIXED_TEMPLATE_MINUTES = "3";
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern CODE_PATTERN = Pattern.compile("^\\d{6}$");
    /**
     * Redis 字符串模板。
     */
    private final StringRedisTemplate stringRedisTemplate;
    /**
     * 配置属性。
     */
    private final SmsVerificationCodeProperties properties;
    /**
     * smsGateway。
     */
    private final SmsGateway smsGateway;

    @Override
    public SmsVerificationCodeSendResult sendCode(SmsVerificationCodeSendCommand command) {
        SmsVerificationCodeSendCommand normalizedCommand = normalizeSendCommand(command);
        String codeKey = buildCacheKey(CODE_CACHE_PREFIX, normalizedCommand);
        String resendKey = buildCacheKey(RESEND_CACHE_PREFIX, normalizedCommand);
        String phoneWindowKey = buildPhoneWindowKey(normalizedCommand.getPhoneNumber());
        String ipDailyKey = buildIpDailyKey(normalizedCommand.getClientIp());

        ensurePhoneWindowLimit(normalizedCommand.getPhoneNumber());
        ensureIpDailyLimit(normalizedCommand.getClientIp());
        if (!acquireResendLock(resendKey)) {
            throw new BusinessException("sms.verification.code.send.too.frequent");
        }

        String verificationCode = generateVerificationCode();
        boolean stateReserved = false;
        try {
            reserveVerificationState(codeKey, phoneWindowKey, ipDailyKey, verificationCode);
            stateReserved = true;

            if (properties.isMockSendEnabled()) {
                log.info("短信验证码进入模拟发送模式，businessType={}, scene={}, phone={}, code={}",
                        normalizedCommand.getBusinessType().getCode(),
                        normalizedCommand.getSceneType().getCode(),
                        SmsMaskingUtils.maskPhone(normalizedCommand.getPhoneNumber()),
                        verificationCode);
                return buildSendResult(normalizedCommand.getPhoneNumber());
            }

            SmsSendRequest smsSendRequest = SmsSendRequest.builder()
                    .sceneType(normalizedCommand.getSceneType())
                    .phoneNumber(normalizedCommand.getPhoneNumber())
                    .templateParam("code", verificationCode)
                    .templateParam("min", FIXED_TEMPLATE_MINUTES)
                    .outId(buildOutId(normalizedCommand))
                    .clientIp(normalizedCommand.getClientIp())
                    .businessKey(codeKey)
                    .build();
            smsGateway.send(smsSendRequest);
            return buildSendResult(normalizedCommand.getPhoneNumber());
        } catch (RuntimeException e) {
            if (stateReserved) {
                rollbackVerificationState(codeKey, resendKey, phoneWindowKey, ipDailyKey);
            } else {
                stringRedisTemplate.delete(resendKey);
            }
            throw e;
        }
    }

    @Override
    public void verifyCode(SmsVerificationCodeVerifyCommand command) {
        SmsVerificationCodeVerifyCommand normalizedCommand = normalizeVerifyCommand(command);
        String codeKey = buildCacheKey(CODE_CACHE_PREFIX, normalizedCommand);
        String storedCode = stringRedisTemplate.opsForValue().get(codeKey);
        if (!StringUtils.hasText(storedCode)) {
            throw new BusinessException("sms.verification.code.expired");
        }

        if (!storedCode.equals(normalizedCommand.getVerificationCode())) {
            throw new BusinessException("invalid.verification.code");
        }

        stringRedisTemplate.delete(codeKey);
    }

    /**
     * 规范化输入参数。
     */
    private SmsVerificationCodeSendCommand normalizeSendCommand(SmsVerificationCodeSendCommand command) {
        if (command == null || command.getBusinessType() == null || command.getSceneType() == null) {
            throw BusinessException.internalError("technical.sms.request.invalid");
        }

        return SmsVerificationCodeSendCommand.builder()
                .businessType(command.getBusinessType())
                .sceneType(command.getSceneType())
                .phoneNumber(normalizePhoneNumber(command.getPhoneNumber()))
                .subjectKey(normalizeSubjectKey(command.getSubjectKey()))
                .clientIp(normalizeClientIp(command.getClientIp()))
                .build();
    }

    /**
     * 规范化输入参数。
     */
    private SmsVerificationCodeVerifyCommand normalizeVerifyCommand(SmsVerificationCodeVerifyCommand command) {
        if (command == null || command.getBusinessType() == null || command.getSceneType() == null) {
            throw BusinessException.internalError("technical.sms.request.invalid");
        }

        return SmsVerificationCodeVerifyCommand.builder()
                .businessType(command.getBusinessType())
                .sceneType(command.getSceneType())
                .phoneNumber(normalizePhoneNumber(command.getPhoneNumber()))
                .subjectKey(normalizeSubjectKey(command.getSubjectKey()))
                .verificationCode(normalizeVerificationCode(command.getVerificationCode()))
                .build();
    }

    /**
     * 规范化手机号参数。
     */
    private String normalizePhoneNumber(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            throw new BusinessException("sms.verification.phone.cannot.be.blank");
        }

        String normalizedPhoneNumber = phoneNumber.trim();
        if (!PHONE_PATTERN.matcher(normalizedPhoneNumber).matches()) {
            throw new BusinessException("sms.verification.phone.invalid");
        }
        return normalizedPhoneNumber;
    }

    /**
     * 规范化验证码参数。
     */
    private String normalizeVerificationCode(String verificationCode) {
        if (!StringUtils.hasText(verificationCode)) {
            throw new BusinessException("sms.verification.code.cannot.be.blank");
        }

        String normalizedVerificationCode = verificationCode.trim();
        if (!CODE_PATTERN.matcher(normalizedVerificationCode).matches()) {
            throw new BusinessException("sms.verification.code.length.invalid");
        }
        return normalizedVerificationCode;
    }

    /**
     * 规范化业务主体标识。
     */
    private String normalizeSubjectKey(String subjectKey) {
        if (!StringUtils.hasText(subjectKey)) {
            return null;
        }
        return subjectKey.trim();
    }

    /**
     * 规范化客户端 IP。
     */
    private String normalizeClientIp(String clientIp) {
        if (!StringUtils.hasText(clientIp)) {
            return null;
        }
        return clientIp.trim();
    }

    /**
     * 构建缓存键。
     */
    private String buildCacheKey(String prefix, SmsVerificationCodeSendCommand command) {
        return buildCacheKey(prefix, command.getBusinessType().getCode(), command.getSceneType().getCode(), command.getPhoneNumber(), command.getSubjectKey());
    }

    /**
     * 根据校验命令构建缓存键。
     */
    private String buildCacheKey(String prefix, SmsVerificationCodeVerifyCommand command) {
        return buildCacheKey(prefix, command.getBusinessType().getCode(), command.getSceneType().getCode(), command.getPhoneNumber(), command.getSubjectKey());
    }

    /**
     * 构建缓存键。
     */
    private String buildCacheKey(String prefix, String businessType, String sceneCode, String phoneNumber, String subjectKey) {
        StringBuilder builder = new StringBuilder(prefix)
                .append(businessType)
                .append(':')
                .append(sceneCode)
                .append(':');
        if (StringUtils.hasText(subjectKey)) {
            builder.append(subjectKey).append(':');
        }
        return builder.append(phoneNumber).toString();
    }

    /**
     * 构建手机号维度的限流键。
     */
    private String buildPhoneWindowKey(String phoneNumber) {
        return PHONE_WINDOW_CACHE_PREFIX + phoneNumber;
    }

    /**
     * 构建 IP 维度的限流键。
     */
    private String buildIpDailyKey(String clientIp) {
        if (!StringUtils.hasText(clientIp)) {
            return null;
        }
        LocalDate currentDay = LocalDate.now(ZoneId.systemDefault());
        return IP_DAILY_CACHE_PREFIX + currentDay + ":" + clientIp;
    }

    /**
     * 申请并校验互斥锁状态。
     */
    private boolean acquireResendLock(String resendKey) {
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(resendKey, "1", properties.getResendIntervalSeconds(), TimeUnit.SECONDS);
        return Boolean.TRUE.equals(locked);
    }

    /**
     * 确保关键约束条件成立。
     */
    private void ensurePhoneWindowLimit(String phoneNumber) {
        if (properties.getPhoneWindowSeconds() <= 0 || properties.getPhoneWindowMaxSends() <= 0) {
            return;
        }

        Long currentCount = readCounter(buildPhoneWindowKey(phoneNumber));
        if (currentCount != null && currentCount >= properties.getPhoneWindowMaxSends()) {
            throw new BusinessException("sms.verification.code.send.phone.limit.exceeded");
        }
    }

    /**
     * 确保关键约束条件成立。
     */
    private void ensureIpDailyLimit(String clientIp) {
        if (!StringUtils.hasText(clientIp) || properties.getIpDailyMaxSends() <= 0) {
            return;
        }

        Long currentCount = readCounter(buildIpDailyKey(clientIp));
        if (currentCount != null && currentCount >= properties.getIpDailyMaxSends()) {
            throw new BusinessException("sms.verification.code.send.ip.limit.exceeded");
        }
    }

    private void reserveVerificationState(
            String codeKey,
            String phoneWindowKey,
            String ipDailyKey,
            String verificationCode
    ) {
        stringRedisTemplate.opsForValue().set(codeKey, verificationCode, FIXED_EXPIRE_SECONDS, TimeUnit.SECONDS);
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

    /**
     * 读取当前计数值。
     */
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
            log.warn("短信验证码计数器值异常，key={}, value={}", key, value);
            stringRedisTemplate.delete(key);
            return null;
        }
    }

    /**
     * 递增计数并刷新有效期。
     */
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

    /**
     * 在失败场景下回退计数。
     */
    private void decrementCounter(String key) {
        if (!StringUtils.hasText(key)) {
            return;
        }

        Long currentCount = stringRedisTemplate.opsForValue().increment(key, -1L);
        if (currentCount == null || currentCount <= 0L) {
            stringRedisTemplate.delete(key);
        }
    }

    /**
     * 生成内部处理结果。
     */
    private String generateVerificationCode() {
        int code = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return String.format("%0" + FIXED_CODE_LENGTH + "d", code);
    }

    /**
     * 计算当天剩余的有效秒数。
     */
    private long resolveCurrentDayExpireSeconds() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime nextDay = now.toLocalDate().plusDays(1).atStartOfDay(now.getZone());
        return Math.max(1L, Duration.between(now, nextDay).getSeconds());
    }

    /**
     * 构建外部流水号。
     */
    private String buildOutId(SmsVerificationCodeSendCommand command) {
        String subject = StringUtils.hasText(command.getSubjectKey()) ? command.getSubjectKey() : "anonymous";
        return command.getBusinessType().getCode()
                + "-" + command.getSceneType().getCode()
                + "-" + subject
                + "-" + System.currentTimeMillis();
    }

    /**
     * 组装发送结果对象。
     */
    private SmsVerificationCodeSendResult buildSendResult(String phoneNumber) {
        return SmsVerificationCodeSendResult.builder()
                .maskedPhoneNumber(SmsMaskingUtils.maskPhone(phoneNumber))
                .expireSeconds(FIXED_EXPIRE_SECONDS)
                .resendIntervalSeconds(properties.getResendIntervalSeconds())
                .build();
    }
}
