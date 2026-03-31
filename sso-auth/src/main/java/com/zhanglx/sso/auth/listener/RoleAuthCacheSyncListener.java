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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/19 14:13
 * @ClassName: RoleAuthCacheSyncListener
 * @Description: 角色鉴权缓存同步监听器
 */
@Slf4j
@Component
public class RoleAuthCacheSyncListener {

    @Resource
    private UserRoleRelationshipMappingMapper userRoleRelationshipMappingMapper;

    /**
     * TransactionalEventListener 确保只有在数据库事务真正 commit 成功后，才执行缓存清理。
     * 配合 @Async 让清理动作跑在虚拟线程中，绝不阻塞主线程响应前端。
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRolePermissionChange(RolePermissionChangedEvent event) {
        Long roleId = event.roleId();
        log.info("检测到角色 [{}] 权限变更，准备批量清理单点登录鉴权缓存 (运行在虚拟线程: {})",
                roleId, Thread.currentThread().isVirtual());
        // todo 更新角色于权限的关联关系后刷新缓存以及通知前台
        try {
            long currentPage = 1;
            // 每次处理 1000 个用户，避免内存被打爆
            long batchSize = 1000;
            boolean hasMore = true;
            long totalProcessed = 0;

            while (hasMore) {
                // 1. 分页查询，且【极其重要】只查询 userId 这一列，拒绝 select *
                Page<UserRoleRelationshipMappingPO> pageParam = Page.of(currentPage, batchSize);
                Page<UserRoleRelationshipMappingPO> pageResult = userRoleRelationshipMappingMapper.selectPage(
                        pageParam,
                        new LambdaQueryWrapperX<UserRoleRelationshipMappingPO>()
                                .select(UserRoleRelationshipMappingPO::getUserId) // 极致压榨 I/O
                                .eq(UserRoleRelationshipMappingPO::getRoleId, roleId)
                );

                List<UserRoleRelationshipMappingPO> records = pageResult.getRecords();

                if (CollectionUtils.isEmpty(records)) {
                    hasMore = false;
                    break;
                }

                // 2. 遍历当前批次，清理缓存
                for (UserRoleRelationshipMappingPO record : records) {
                    clearUserAuthCache(record.getUserId());
                }

                totalProcessed += records.size();

                // 3. 判断是否还有下一页
                if (records.size() < batchSize) {
                    hasMore = false;
                } else {
                    currentPage++;
                }
            }

            log.info("角色 [{}] 权限缓存清理完成，共影响 {} 个在线/离线用户", roleId, totalProcessed);
        } catch (Exception e) {
            log.error("角色 [{}] 权限缓存清理失败，需人工介入排查", roleId, e);
        }
    }

    /**
     * 监听角色绑定的用户变更事件
     *
     * @Async: 异步执行，不要阻塞主业务(绑定用户)的响应时间
     * phase = TransactionPhase.AFTER_COMMIT: 【极其重要】必须等待主事务成功提交后，才执行缓存清理
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRoleUsersChanged(RoleUsersChangedEvent event) {
        Long roleId = event.roleId();
        Set<Long> toAddUsers = event.diffResult().toAdd();
        Set<Long> toDeleteUsers = event.diffResult().toDelete();

        log.info("触发角色关联用户变更事件: roleId={}, 新增关联数={}, 移除关联数={}",
                roleId, toAddUsers.size(), toDeleteUsers.size());

        // 1. 处理被移除该角色的用户：清理 Sa-Token 中的权限/角色缓存
        for (Long userId : toDeleteUsers) {
            clearUserAuthCache(userId);
            // 严苛的安全场景下，也可以直接踢下线强制重新登录：
            // StpUtil.kickout(userId);
        }

        // 2. 处理被新增该角色的用户：同样清理缓存，促使下次校验时重新拉取最新权限
        for (Long userId : toAddUsers) {
            clearUserAuthCache(userId);
        }
    }

    /**
     * 清理用户的 Sa-Token 权限和角色缓存
     */
    private void clearUserAuthCache(Long userId) {
        try {
            // 注意：这里的 Key 需要根据你在 Sa-Token 中配置的缓存键来写
            // 如果你在 StpInterface 实现类中没有做额外缓存，Sa-Token 默认是不缓存权限的（每次查库）。
            // 如果你使用了 Sa-Token-Dao-Redis 且开启了缓存，可以通过 Session 清理：
            StpUtil.getSessionByLoginId(userId).delete("Role_List");
            StpUtil.getSessionByLoginId(userId).delete("Permission_List");

            // 补充提示：如果您在前端做了菜单/按钮路由的本地缓存，
            // 也可以在这里结合 WebSocket 或 SSE，向该 userId 推送一条 "AUTH_CHANGED" 的消息，
            // 让前端静默刷新一次路由表。

            log.debug("已清理用户权限缓存: userId={}", userId);
        } catch (Exception e) {
            log.error("清理用户权限缓存失败: userId={}", userId, e);
        }
    }

}
