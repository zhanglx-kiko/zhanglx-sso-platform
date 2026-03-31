package com.zhanglx.sso.auth.event;

import org.springframework.context.ApplicationEvent;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/27 10:20
 * @ClassName: PermissionChangedEvent
 * @Description: 权限数据变更事件
 */
public class PermissionChangedEvent extends ApplicationEvent {

    private final String taskId; // 触发本次变更的任务ID（可选，用于日志追踪）

    public PermissionChangedEvent(Object source, String taskId) {
        super(source);
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }

}
