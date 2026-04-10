package com.zhanglx.sso.auth.listener;

import com.zhanglx.sso.auth.constants.PermissionCacheConstants;
import com.zhanglx.sso.auth.event.PermissionChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class PermissionCacheCleanListener {

    private final StringRedisTemplate stringRedisTemplate;
    private final CacheManager cacheManager;

    public PermissionCacheCleanListener(
            StringRedisTemplate stringRedisTemplate,
            ObjectProvider<CacheManager> cacheManagerProvider) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.cacheManager = cacheManagerProvider.getIfAvailable();
    }

    @Async
    @EventListener
    public void handlePermissionChangedEvent(PermissionChangedEvent event) {
        log.info("收到权限变更事件，开始清理权限缓存，taskId={}", event.getTaskId());

        try {
            if (cacheManager != null && cacheManager.getCache(PermissionCacheConstants.PERMISSION_TREE_CACHE) != null) {
                Objects.requireNonNull(cacheManager.getCache(PermissionCacheConstants.PERMISSION_TREE_CACHE)).clear();
                log.info("已清理 {} 缓存", PermissionCacheConstants.PERMISSION_TREE_CACHE);
            }

            clearRedisKeysSafely("sso:auth:permission:*");
            log.info("权限缓存清理完成");
        } catch (Exception e) {
            log.error("权限缓存清理失败", e);
        }
    }

    /**
     * 安全清理匹配的 Redis 缓存键。
     */
    private void clearRedisKeysSafely(String pattern) {
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(1000).build();
        List<String> keysToDelete = new ArrayList<>();

        stringRedisTemplate.execute((RedisConnection connection) -> {
            try (Cursor<byte[]> cursor = connection.keyCommands().scan(options)) {
                while (cursor.hasNext()) {
                    keysToDelete.add(new String(cursor.next(), StandardCharsets.UTF_8));
                    if (keysToDelete.size() >= 500) {
                        stringRedisTemplate.delete(keysToDelete);
                        keysToDelete.clear();
                    }
                }

                if (!keysToDelete.isEmpty()) {
                    stringRedisTemplate.delete(keysToDelete);
                }
            } catch (Exception e) {
                log.error("扫描 Redis 权限缓存失败", e);
            }

            return null;
        });
    }
}