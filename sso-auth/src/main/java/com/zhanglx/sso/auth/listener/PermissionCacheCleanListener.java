package com.zhanglx.sso.auth.listener;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/27 10:25
 * @ClassName: PermissionCacheCleanListener
 * @Description: 权限缓存清理监听器
 */
@Slf4j
@Component
public class PermissionCacheCleanListener {

    private final StringRedisTemplate stringRedisTemplate;
    private final CacheManager cacheManager; // 用于清理 @Cacheable 产生的 Spring Cache

    public PermissionCacheCleanListener(
            StringRedisTemplate stringRedisTemplate,
            ObjectProvider<CacheManager> cacheManagerProvider) { // 使用 ObjectProvider 包装
        this.stringRedisTemplate = stringRedisTemplate;
        // 如果容器里有 CacheManager 就拿出来，没有就是 null，程序不会报错
        this.cacheManager = cacheManagerProvider.getIfAvailable();
    }

    /**
     * 监听权限变更事件
     * 使用 @Async 让清理动作在独立的虚拟线程中执行，不阻塞主业务流程
     */
    @Async
    @EventListener
    public void handlePermissionChangedEvent(PermissionChangedEvent event) {
        log.info("接收到权限变更事件 [taskId: {}]，开始执行全网权限缓存清理...", event.getTaskId());

        try {
            // ==========================================
            // 1. 清理前端需要的“权限树”缓存 (Spring Cache)
            // ==========================================
            // 对应你之前代码里的 @CacheEvict(value = "PermissionTree", allEntries = true)
            if (cacheManager != null && cacheManager.getCache("PermissionTree") != null) {
                Objects.requireNonNull(cacheManager.getCache("PermissionTree")).clear();
                log.info("-> 成功清理 Spring Cache [PermissionTree]");
            }

            // ==========================================
            // 2. 清理 Sa-Token 缓存的用户底层权限标识 (Redis)
            // ==========================================
            // 假设你在 StpInterfaceImpl 中把用户权限缓存到了 "sso:user:permissions:{userId}"
            String cacheKeyPattern = "sso:user:permissions:*";
            clearRedisKeysSafely(cacheKeyPattern);

            log.info("-> 全网权限缓存清理完毕！");

        } catch (Exception e) {
            log.error("权限缓存清理失败，可能导致部分用户权限未实时刷新！", e);
            // 生产环境建议这里接入告警系统 (如钉钉/飞书机器人)
        }
    }

    /**
     * Redis 模糊删除 (基于 SCAN 非阻塞命令)
     */
    private void clearRedisKeysSafely(String pattern) {
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(1000).build();
        List<String> keysToDelete = new ArrayList<>();

        // 推荐使用 execute 回调，Spring 会自动管理底层 RedisConnection 的生命周期，彻底杜绝连接泄漏
        stringRedisTemplate.execute((RedisConnection connection) -> {

            // 【关键修复】：使用 keyCommands().scan() 替代已弃用的直接 scan()
            try (Cursor<byte[]> cursor = connection.keyCommands().scan(options)) {
                while (cursor.hasNext()) {
                    keysToDelete.add(new String(cursor.next()));

                    // 批处理删除，防止一次性删除过多导致的瞬间网络拥堵
                    if (keysToDelete.size() >= 500) {
                        stringRedisTemplate.delete(keysToDelete);
                        keysToDelete.clear();
                    }
                }

                // 删除最后一批
                if (!keysToDelete.isEmpty()) {
                    stringRedisTemplate.delete(keysToDelete);
                }
            } catch (Exception e) {
                log.error("执行 Redis Scan 清理缓存时发生异常", e);
            }

            return null; // execute 需要返回值，这里不需要返回结果直接给 null 即可
        });
    }

}
