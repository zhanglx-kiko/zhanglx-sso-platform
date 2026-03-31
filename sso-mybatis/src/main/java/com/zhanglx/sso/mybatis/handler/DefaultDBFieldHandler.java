package com.zhanglx.sso.mybatis.handler;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
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
 * @Description:
 */
@Slf4j
public class DefaultDBFieldHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        if (log.isDebugEnabled()) {
            log.info("db start insert fill ....");
        }
        if (Objects.nonNull(metaObject)
                && metaObject.getOriginalObject() instanceof BasePO) {
            LocalDateTime current = LocalDateTime.now();
            Long userId = StpUtil.getLoginIdAsLong();
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
        if (Objects.nonNull(metaObject)
                && metaObject.getOriginalObject() instanceof BasePO) {
            metaObject.setValue("updateBy", StpUtil.getLoginIdAsLong());
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

}
