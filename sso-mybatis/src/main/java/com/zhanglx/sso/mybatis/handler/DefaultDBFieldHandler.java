package com.zhanglx.sso.mybatis.handler;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.zhanglx.sso.core.utils.satoken.StpMemberUtil;
import com.zhanglx.sso.mybatis.domain.po.BasePO;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/5 16:36
 * @ClassName: DefaultDBFieldHandler
 * @Description: 自动填充审计字段
 */
@Slf4j
public class DefaultDBFieldHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        if (log.isDebugEnabled()) {
            log.info("db start insert fill ....");
        }

        if (Objects.nonNull(metaObject) && metaObject.getOriginalObject() instanceof BasePO) {
            LocalDateTime current = LocalDateTime.now();
            Long userId = getCurrentUserId();
            this.strictInsertFill(metaObject, "createBy", Long.class, userId);
            this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, current);
            this.strictInsertFill(metaObject, "updateBy", Long.class, userId);
            this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, current);
            this.strictInsertFill(metaObject, "delFlag", Long.class, 0L);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if (log.isDebugEnabled()) {
            log.info("db start update fill ....");
        }

        if (Objects.nonNull(metaObject) && metaObject.getOriginalObject() instanceof BasePO) {
            metaObject.setValue("updateBy", getCurrentUserId());
            metaObject.setValue("updateTime", LocalDateTime.now());
        }
    }

    @Override
    public MetaObjectHandler strictFillStrategy(MetaObject metaObject, String fieldName, Supplier<?> fieldVal) {
        // 原方法，当填充字段不是null时，不会进行填充。即前端更新时携带了旧的update信息，就不会填充update
        if (metaObject.getValue(fieldName) == null) {
            Object obj = fieldVal.get();
            if (Objects.nonNull(obj)) {
                metaObject.setValue(fieldName, obj);
            }
        }

        return this;
    }

    /**
     * 安全获取当前登录人ID的核心逻辑
     */
    private Long getCurrentUserId() {
        try {
            // 1. 尝试获取 B端 (Admin) 的登录状态
            if (StpUtil.isLogin()) {
                return StpUtil.getLoginIdAsLong();
            }

            // 2. 尝试获取 C端 (Member) 的登录状态
            // 注意：这里需要替换为你自己定义的 C端 StpMemberUtil 工具类
            if (StpMemberUtil.isLogin()) {
                return StpMemberUtil.getLoginIdAsLong();
            }
        } catch (Exception e) {
            log.warn("获取当前登录用户ID异常，采用默认值: {}", e.getMessage());
        }

        // 3. 如果都没有登录（例如：新用户注册、系统内部定时任务、第三方回调接收），默认返回 0L
        return 0L;
    }

}
