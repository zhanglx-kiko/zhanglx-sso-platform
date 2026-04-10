package com.zhanglx.sso.auth.service.runtime;

import com.zhanglx.sso.auth.config.ConfigRuntimeCacheProperties;
import com.zhanglx.sso.auth.domain.po.ConfigPO;
import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.auth.exception.AuthManageErrorCode;
import com.zhanglx.sso.auth.mapper.ConfigMapper;
import com.zhanglx.sso.core.config.runtime.SystemConfigProvider;
import com.zhanglx.sso.core.exception.SystemConfigException;
import com.zhanglx.sso.core.utils.AssertUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于数据库的系统运行时配置提供者。
 * 这里统一负责配置查库、状态校验、本地缓存和缓存刷新。
 */
@Service
@Primary
@RequiredArgsConstructor
public class DatabaseSystemConfigProvider implements SystemConfigProvider {

    /**
     * 最小缓存时间，避免异常配置把数据库打穿。
     */
    private static final long MIN_CACHE_TTL_MILLIS = 1000L;

    private final ConfigMapper configMapper;
    private final ConfigRuntimeCacheProperties cacheProperties;

    /**
     * 本地配置缓存。
     */
    private final ConcurrentMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /**
     * 单键加载锁，避免高并发下重复查库。
     */
    private final ConcurrentMap<String, Object> keyLocks = new ConcurrentHashMap<>();

    @Override
    public Optional<String> getString(String configKey) {
        CacheEntry cacheEntry = getOrLoad(configKey);
        return cacheEntry.isAvailable() ? Optional.of(cacheEntry.configValue()) : Optional.empty();
    }

    @Override
    public Optional<String> getSensitiveString(String configKey) {
        return getString(configKey);
    }

    @Override
    public String getRequiredString(String configKey) {
        CacheEntry cacheEntry = getOrLoad(configKey);
        return switch (cacheEntry.state()) {
            case READY -> cacheEntry.configValue();
            case MISSING -> throw SystemConfigException.missing(configKey);
            case BLANK -> throw SystemConfigException.blank(configKey);
            case DISABLED -> throw SystemConfigException.disabled(configKey);
        };
    }

    @Override
    public String getRequiredSensitiveString(String configKey) {
        return getRequiredString(configKey);
    }

    @Override
    public void refresh(String configKey) {
        if (!StringUtils.hasText(configKey)) {
            return;
        }
        cache.remove(configKey.trim());
        keyLocks.remove(configKey.trim());
    }

    @Override
    public void refreshAll() {
        cache.clear();
        keyLocks.clear();
    }

    /**
     * 按键读取原始配置记录，供管理后台和脱敏转换复用。
     */
    public ConfigPO getRawConfig(String configKey) {
        AssertUtils.notBlank(configKey, AuthManageErrorCode.CONFIG_KEY_REQUIRED);
        return configMapper.selectOne(ConfigPO::getConfigKey, configKey.trim());
    }

    /**
     * 加载缓存，如果本地没有或已过期则重新查库。
     */
    private CacheEntry getOrLoad(String configKey) {
        AssertUtils.notBlank(configKey, AuthManageErrorCode.CONFIG_KEY_REQUIRED);
        String normalizedKey = configKey.trim();
        CacheEntry cached = cache.get(normalizedKey);
        if (cached != null && !cached.isExpired()) {
            return cached;
        }

        Object keyLock = keyLocks.computeIfAbsent(normalizedKey, unused -> new Object());
        synchronized (keyLock) {
            cached = cache.get(normalizedKey);
            if (cached != null && !cached.isExpired()) {
                return cached;
            }

            CacheEntry loaded = loadFromDatabase(normalizedKey);
            cache.put(normalizedKey, loaded);
            return loaded;
        }
    }

    /**
     * 统一封装数据库加载逻辑。
     */
    private CacheEntry loadFromDatabase(String configKey) {
        ConfigPO configPO = configMapper.selectOne(ConfigPO::getConfigKey, configKey);
        long expireAt = System.currentTimeMillis() + Math.max(MIN_CACHE_TTL_MILLIS, cacheProperties.getCacheTtlSeconds() * 1000L);
        if (configPO == null) {
            return new CacheEntry(ConfigState.MISSING, null, expireAt);
        }
        if (!EnableStatusEnum.isEnabled(configPO.getStatus())) {
            return new CacheEntry(ConfigState.DISABLED, null, expireAt);
        }
        if (!StringUtils.hasText(configPO.getConfigValue())) {
            return new CacheEntry(ConfigState.BLANK, null, expireAt);
        }
        return new CacheEntry(ConfigState.READY, configPO.getConfigValue().trim(), expireAt);
    }

    /**
     * 缓存条目。
     */
    private record CacheEntry(ConfigState state, String configValue, long expireAt) {
        private boolean isExpired() {
            return System.currentTimeMillis() >= expireAt;
        }

        private boolean isAvailable() {
            return state == ConfigState.READY && StringUtils.hasText(configValue);
        }
    }

    /**
     * 缓存状态。
     */
    private enum ConfigState {
        READY,
        MISSING,
        BLANK,
        DISABLED
    }
}
