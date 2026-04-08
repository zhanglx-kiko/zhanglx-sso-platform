package com.zhanglx.sso.web.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestProtectionStore {

    private static final DefaultRedisScript<List> FIXED_WINDOW_RATE_LIMIT_SCRIPT = new DefaultRedisScript<>();

    static {
        FIXED_WINDOW_RATE_LIMIT_SCRIPT.setScriptText("""
                local limit = tonumber(ARGV[1])
                local windowMillis = tonumber(ARGV[2])
                local current = redis.call('INCR', KEYS[1])
                if current == 1 then
                    redis.call('PEXPIRE', KEYS[1], windowMillis)
                end
                local ttl = redis.call('PTTL', KEYS[1])
                if ttl < 0 then
                    ttl = windowMillis
                end
                local allowed = 0
                if current <= limit then
                    allowed = 1
                end
                local remaining = limit - current
                if remaining < 0 then
                    remaining = 0
                end
                local resetSeconds = math.floor((ttl + 999) / 1000)
                return {allowed, current, remaining, resetSeconds}
                """);
        FIXED_WINDOW_RATE_LIMIT_SCRIPT.setResultType(List.class);
    }

    private final ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider;

    private final ConcurrentMap<String, Long> localRepeatSubmitCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, LocalRateLimitCounter> localRateLimitCache = new ConcurrentHashMap<>();

    public boolean tryAcquireRepeatSubmit(String lockKey, Duration window, boolean localFallbackEnabled) {
        StringRedisTemplate redisTemplate = stringRedisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            try {
                Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", window);
                return Boolean.TRUE.equals(success);
            } catch (Exception ex) {
                log.warn("repeat submit redis unavailable, fallbackEnabled={}, key={}", localFallbackEnabled, lockKey, ex);
            }
        }
        return localFallbackEnabled && tryAcquireLocalRepeatSubmit(lockKey, window);
    }

    public RateLimitDecision acquireRateLimit(String key, long limit, Duration window, boolean localFallbackEnabled) {
        StringRedisTemplate redisTemplate = stringRedisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            try {
                List<?> result = redisTemplate.execute(
                        FIXED_WINDOW_RATE_LIMIT_SCRIPT,
                        Collections.singletonList(key),
                        String.valueOf(limit),
                        String.valueOf(window.toMillis())
                );
                if (result != null && result.size() >= 4) {
                    boolean allowed = asLong(result.get(0)) == 1L;
                    long current = asLong(result.get(1));
                    long remaining = asLong(result.get(2));
                    long resetSeconds = asLong(result.get(3));
                    return new RateLimitDecision(allowed, limit, remaining, resetSeconds, current);
                }
            } catch (Exception ex) {
                log.warn("rate limit redis unavailable, fallbackEnabled={}, key={}", localFallbackEnabled, key, ex);
            }
        }
        return localFallbackEnabled
                ? acquireLocalRateLimit(key, limit, window)
                : new RateLimitDecision(true, limit, limit, Math.max(1L, window.toSeconds()), 0L);
    }

    private boolean tryAcquireLocalRepeatSubmit(String lockKey, Duration window) {
        long now = System.currentTimeMillis();
        long expireAt = now + window.toMillis();
        localRepeatSubmitCache.entrySet().removeIf(entry -> entry.getValue() <= now);
        Long existingExpireAt = localRepeatSubmitCache.putIfAbsent(lockKey, expireAt);
        if (existingExpireAt == null) {
            return true;
        }
        return existingExpireAt <= now && localRepeatSubmitCache.replace(lockKey, existingExpireAt, expireAt);
    }

    private RateLimitDecision acquireLocalRateLimit(String key, long limit, Duration window) {
        long now = System.currentTimeMillis();
        long expireAt = now + window.toMillis();
        localRateLimitCache.entrySet().removeIf(entry -> entry.getValue().expireAt <= now);
        LocalRateLimitCounter counter = localRateLimitCache.compute(key, (ignored, existing) -> {
            if (existing == null || existing.expireAt <= now) {
                return new LocalRateLimitCounter(1L, expireAt);
            }
            existing.count++;
            return existing;
        });
        long current = counter.count;
        long remaining = Math.max(0L, limit - current);
        long resetSeconds = Math.max(1L, ((counter.expireAt - now) + 999L) / 1000L);
        return new RateLimitDecision(current <= limit, limit, remaining, resetSeconds, current);
    }

    private long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private static final class LocalRateLimitCounter {

        private long count;
        private long expireAt;

        private LocalRateLimitCounter(long count, long expireAt) {
            this.count = count;
            this.expireAt = expireAt;
        }
    }
}
