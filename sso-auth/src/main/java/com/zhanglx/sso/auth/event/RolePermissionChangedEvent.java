package com.zhanglx.sso.auth.event;

/**
 * 作者：Zhang L X
 * 创建时间：2026/3/19 11:32
 * 类名：Role权限变更事件
 * 说明：角色权限变更事件
 */
public record RolePermissionChangedEvent(Long roleId) {
}