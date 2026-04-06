package com.zhanglx.sso.auth.listener;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhanglx.sso.auth.domain.po.UserRoleRelationshipMappingPO;
import com.zhanglx.sso.auth.event.RolePermissionChangedEvent;
import com.zhanglx.sso.auth.event.RoleUsersChangedEvent;
import com.zhanglx.sso.auth.mapper.UserRoleRelationshipMappingMapper;
import com.zhanglx.sso.mybatis.query.LambdaQueryWrapperX;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class RoleAuthCacheSyncListener {

    private static final String ROLE_CACHE_PREFIX = "sso:auth:role:";
    private static final String PERMISSION_CACHE_PREFIX = "sso:auth:permission:";

    @Resource
    private UserRoleRelationshipMappingMapper userRoleRelationshipMappingMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRolePermissionChange(RolePermissionChangedEvent event) {
        Long roleId = event.roleId();
        log.info("检测到角色 [{}] 权限变更，开始清理鉴权缓存，虚拟线程={}", roleId, Thread.currentThread().isVirtual());
        try {
            long currentPage = 1;
            long batchSize = 1000;
            boolean hasMore = true;
            long totalProcessed = 0;

            while (hasMore) {
                Page<UserRoleRelationshipMappingPO> pageParam = Page.of(currentPage, batchSize);
                Page<UserRoleRelationshipMappingPO> pageResult = userRoleRelationshipMappingMapper.selectPage(
                        pageParam,
                        new LambdaQueryWrapperX<UserRoleRelationshipMappingPO>()
                                .select(UserRoleRelationshipMappingPO::getUserId)
                                .eq(UserRoleRelationshipMappingPO::getRoleId, roleId)
                );

                List<UserRoleRelationshipMappingPO> records = pageResult.getRecords();
                if (CollectionUtils.isEmpty(records)) {
                    hasMore = false;
                    break;
                }

                for (UserRoleRelationshipMappingPO record : records) {
                    clearUserAuthCache(record.getUserId());
                }

                totalProcessed += records.size();
                if (records.size() < batchSize) {
                    hasMore = false;
                } else {
                    currentPage++;
                }
            }

            log.info("角色 [{}] 的权限缓存清理完成，共处理 {} 个用户", roleId, totalProcessed);
        } catch (Exception e) {
            log.error("角色 [{}] 的权限缓存清理失败", roleId, e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRoleUsersChanged(RoleUsersChangedEvent event) {
        Long roleId = event.roleId();
        Set<Long> toAddUsers = event.diffResult().toAdd();
        Set<Long> toDeleteUsers = event.diffResult().toDelete();

        log.info("角色关联用户发生变化，roleId={}, 新增={}, 移除={}", roleId, toAddUsers.size(), toDeleteUsers.size());

        for (Long userId : toDeleteUsers) {
            clearUserAuthCache(userId);
        }

        for (Long userId : toAddUsers) {
            clearUserAuthCache(userId);
        }
    }

    private void clearUserAuthCache(Long userId) {
        try {
            StpUtil.getSessionByLoginId(userId).delete("Role_List");
            StpUtil.getSessionByLoginId(userId).delete("Permission_List");
            List<String> cacheKeys = new ArrayList<>();
            cacheKeys.add(ROLE_CACHE_PREFIX + StpUtil.TYPE + ":" + userId);
            cacheKeys.add(PERMISSION_CACHE_PREFIX + StpUtil.TYPE + ":" + userId);
            stringRedisTemplate.delete(cacheKeys);
            log.debug("已清理用户权限缓存，userId={}", userId);
        } catch (Exception e) {
            log.error("清理用户权限缓存失败，userId={}", userId, e);
        }
    }
}
