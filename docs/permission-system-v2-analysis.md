# 权限系统 V2 梳理与接口设计

日期：2026-04-04

## 1. 结论摘要

基于 `V2__init_schema.sql`，当前目标模型已经从 V1 的“单一用户表 + 统一权限表”升级为“B/C 双主体 + 应用隔离 + RBAC + 数据权限”框架，但代码实现仍大量停留在 V1 结构，导致当前仓库存在三类核心问题：

1. 数据模型与代码严重漂移：V2 使用 `t_sys_user` / `t_member_user` / `t_auth_user_role` / `t_auth_role_permission`，当前代码仍在访问 `t_auth_user` / `t_auth_user_role_mapping` / `t_auth_role_permission_mapping`。
2. B 端和 C 端路由虽然开始分离，但业务服务、权限码、缓存、网关放行、登录态使用的仍然主要是 B 端逻辑。
3. V2 对 B 端 RBAC 已经具备完整骨架，对 C 端仅完成了账号主体拆分，尚未补齐“会员角色/权限映射、应用授权、接口粒度控制”。

这意味着当前最合理的落地方式不是继续在现有 Controller 上打补丁，而是先以 V2 为准重新定义 B/C 两套接口边界，并补齐 C 端缺失的权限模型。

## 2. V2 表结构分析

### 2.1 模型分层

| 层级 | 表 | 作用 | 面向端 |
| --- | --- | --- | --- |
| 应用层 | `t_sso_app` | 定义接入应用、应用编码、应用用户类型 | B/C 共用 |
| B 端主体层 | `t_sys_user`、`t_sys_user_social` | 后台员工账号、第三方登录绑定 | B |
| C 端主体层 | `t_member_user`、`t_member_social` | 电商会员账号、第三方登录绑定 | C |
| 组织层 | `t_auth_dept`、`t_auth_post`、`t_auth_user_post` | 部门、岗位、用户岗位关系 | B |
| 权限核心层 | `t_auth_role`、`t_auth_permission`、`t_auth_user_role`、`t_auth_role_permission`、`t_auth_role_dept`、`t_auth_user_app` | RBAC、数据权限、应用白名单 | 设计上偏 B，理论上可扩展到 C |
| 支撑层 | `t_sys_dict_type`、`t_sys_dict_data`、`t_sys_config` | 字典、系统参数 | B |
| 日志层 | `t_auth_login_log`、`t_auth_operate_log` | 登录日志、操作审计 | B/C 共用 |

### 2.2 V2 的设计意图

#### 2.2.1 应用隔离

- `t_sso_app.app_code` 是权限域的第一层隔离键。
- `t_auth_role.app_code` 和 `t_auth_permission.app_code` 说明角色、权限必须归属某个应用。
- `t_auth_user_app` 说明 B 端用户登录后，还要校验其是否被授权访问该应用。

结论：V2 不是单系统权限模型，而是“SSO + 多应用权限域”模型。

#### 2.2.2 B/C 主体拆分

- B 端主体使用 `t_sys_user`。
- C 端主体使用 `t_member_user`。
- `t_sso_app.user_type` 用于标记某应用面向的是 `sys` 还是 `member`。

结论：登录主体已经拆开，后续权限校验和接口路由也必须跟着拆开。

#### 2.2.3 B 端 RBAC 与数据权限

- `t_auth_role.data_scope` 已支持“全部、本部门及以下、本部门、本人、自定义”。
- `t_auth_role_dept` 已支持自定义部门授权。
- `t_auth_permission.type` 已支持平台/模块/菜单/按钮/接口五级粒度。

结论：B 端权限模型在 V2 中是完整的，缺的是代码落地。

#### 2.2.4 C 端权限骨架不完整

V2 虽然新增了 `t_member_user` / `t_member_social`，但没有与之显式对应的：

- `member -> role` 映射表
- `member -> app` 映射表
- 面向会员的权限管理接口

如果继续沿用 `t_auth_user_role` / `t_auth_user_app`，则必须引入“主体类型”字段，否则仅靠 `user_id` 无法明确区分是 `t_sys_user.id` 还是 `t_member_user.id`。

结论：V2 目前更像“B 端完整、C 端只拆账号未拆权限”。

### 2.3 关键字段含义

| 字段 | 所在表 | 作用 |
| --- | --- | --- |
| `app_code` | `t_sso_app`、`t_auth_role`、`t_auth_permission`、`t_auth_user_app` | 应用隔离主键 |
| `user_type` | `t_sso_app`、`t_sys_user` | 标记应用/用户类型 |
| `data_scope` | `t_auth_role` | B 端数据范围控制 |
| `type` | `t_auth_permission` | 权限粒度：平台/模块/菜单/按钮/接口 |
| `status` | 主体表、角色表、权限表 | 启用/禁用控制 |
| `del_flag` | 全局 | 逻辑删除 |

## 3. 当前接口现状评估

### 3.1 当前 Controller 清单

当前仓库中只有 6 个 Controller：

- `AuthSysController`
- `AuthMemberController`
- `UserSysController`
- `UserMemberController`
- `RoleController`
- `PermissionController`

说明当前接口只覆盖了认证、用户、角色、权限，尚未覆盖 V2 中已经存在的应用、部门、岗位、数据权限等领域。

### 3.2 B 端接口清单

以下接口属于 B 端后台系统接口，或实际上只能工作在 B 端上下文中：

| 方法 | 路径 | 功能 | 当前访问控制 | 评估 |
| --- | --- | --- | --- | --- |
| POST | `/apis/v1/auth/s/login` | B 端登录 | 公开 | 正常 |
| POST | `/apis/v1/auth/s/logout` | B 端退出 | `@SaCheckLogin` | 正常 |
| POST | `/apis/v1/auth/s/user/update/password` | 修改本人密码 | `@SaCheckLogin` | 正常 |
| POST | `/apis/v1/auth/s/user/reset-password/{userId}` | 管理员重置密码 | `user:reset` | 正常 |
| POST | `/apis/v1/auth/s/forgot-password` | 忘记密码 | 公开 | 正常 |
| POST | `/apis/v1/user/s/add` | 新增用户 | 无 | 缺少 `user:add` |
| POST | `/apis/v1/user/s/update/info` | 更新用户 | `user:edit` | 正常 |
| DELETE | `/apis/v1/user/s/remove/{userId}` | 删除用户 | `user:remove` | 正常 |
| POST | `/apis/v1/user/s/list` | 分页查询用户 | `user:list` | 正常 |
| POST | `/apis/v1/user/s/disable/{userId}` | 启停用户 | `user:disable` | 正常 |
| POST | `/apis/v1/roles` | 新增角色 | `role:add` | 实际属于 B 端 |
| PUT | `/apis/v1/roles/{id}` | 修改角色 | `role:edit` | 实际属于 B 端 |
| DELETE | `/apis/v1/roles/{id}` | 删除角色 | `role:remove` | 实际属于 B 端 |
| DELETE | `/apis/v1/roles/batch` | 批量删除角色 | `role:remove` | 实际属于 B 端 |
| POST | `/apis/v1/roles/page` | 角色分页 | `role:list` | 实际属于 B 端 |
| GET | `/apis/v1/roles/{roleId}` | 角色详情 | `role:view` | 实际属于 B 端 |
| GET | `/apis/v1/roles/{roleId}/users` | 查询角色绑定用户 | `role:view` | 实际属于 B 端 |
| POST | `/apis/v1/roles/{roleId}/users` | 角色绑定用户 | `role:bind-user` | 实际属于 B 端 |
| POST | `/apis/v1/roles/{roleId}/permissions` | 角色分配权限 | `role:assign-permission` | 实际属于 B 端 |
| GET | `/apis/v1/roles/my-roles` | 查询当前用户角色 | `@SaCheckPermission` 空值 | 注解错误，应改为登录校验 |
| GET | `/apis/v1/roles/user/{userId}` | 查询用户角色 | `role:view` | 实际属于 B 端 |
| POST | `/apis/v1/permissions` | 新增权限 | `permission:add` | 实际属于 B 端 |
| PUT | `/apis/v1/permissions/{id}` | 修改权限 | `permission:edit` | 实际属于 B 端 |
| DELETE | `/apis/v1/permissions/{id}` | 删除权限 | `permission:remove` | 实际属于 B 端 |
| DELETE | `/apis/v1/permissions/batch` | 批量删除权限 | `permission:remove` | 实际属于 B 端 |
| GET | `/apis/v1/permissions/tree` | 查询权限树 | `permission:list` | 实际属于 B 端 |
| POST | `/apis/v1/permissions/by-identification` | 按标识查询权限 | `permission:list` | 实际属于 B 端 |
| POST | `/apis/v1/permissions/import` | 导入权限 | 无 | 缺少 `permission:import` |
| GET | `/apis/v1/permissions/import/progress/{taskId}` | 导入进度 | 无 | 缺少登录与任务归属校验 |
| GET | `/apis/v1/permissions/export` | 导出权限 | 无 | 缺少 `permission:export` |
| GET | `/apis/v1/permissions/export/progress/{taskId}` | 导出进度 | 无 | 缺少登录与任务归属校验 |

### 3.3 C 端接口清单

以下接口属于 C 端电商会员接口，但实现并未真正独立：

| 方法 | 路径 | 功能 | 当前访问控制 | 评估 |
| --- | --- | --- | --- | --- |
| POST | `/apis/v1/auth/m/wechat/login` | 微信登录 | 公开 | 只接了社交登录，未接手机/密码 |
| POST | `/apis/v1/auth/m/logout` | 会员退出 | 无 | 应增加 `StpMemberUtil` 登录校验 |
| POST | `/apis/v1/auth/m/user/update/password` | 修改密码 | `@SaCheckLogin` | 使用了 `StpUtil`，且未真正调用服务 |
| POST | `/apis/v1/auth/m/forgot-password` | 忘记密码 | 无 | 空实现 |
| POST | `/apis/v1/user/m/add` | 新增用户 | 无 | 复用 B 端 `UserService`，主体错误 |
| POST | `/apis/v1/user/m/update/info` | 更新用户 | `user:edit` | 权限码仍是 B 端 `user:*` |
| DELETE | `/apis/v1/user/m/remove/{userId}` | 注销用户 | `user:remove` | 仍使用 B 端权限与 B 端用户服务 |
| POST | `/apis/v1/user/m/list` | 查询用户列表 | `user:list` | 应该是会员列表或“我的信息”，现实现错误 |

### 3.4 以 sys 相关接口为标准的完整性评估

以当前 B 端 `auth/s` 与 `user/s` 为基准，C 端应至少具备对等的认证闭环和主体管理闭环，但当前缺口非常明显：

| 能力项 | B 端现状 | C 端现状 | 评估 |
| --- | --- | --- | --- |
| 登录 | 已有账号密码登录 | 仅微信登录 | 缺失手机/密码/验证码登录 |
| 退出 | 已有 | 已有 | C 端未绑定正确登录类型 |
| 修改密码 | 已有 | 路由存在但未落地 | 缺失 |
| 忘记密码 | 已有 | 空实现 | 缺失 |
| 新增主体 | 已有 | 复用 B 端用户新增 | 主体错误 |
| 更新主体 | 已有 | 复用 B 端用户更新 | 主体错误 |
| 删除/注销主体 | 已有 | 复用 B 端用户删除 | 主体错误 |
| 列表查询 | 已有 | 复用 B 端用户列表 | 主体错误 |
| 角色管理 | 已有 | 没有 | 缺失 |
| 权限管理 | 已有 | 没有 | 缺失 |
| 当前登录态权限回显 | 没有 `me/permissions/routes` | 没有 | B/C 均缺失 |

## 4. 当前实现中的关键缺口

### 4.1 代码仍绑定 V1 表结构

当前代码使用的是旧模型：

| V2 表/字段 | 当前代码使用 | 结果 |
| --- | --- | --- |
| `t_sys_user` | `UserPO -> t_auth_user` | B 端主体表不匹配 |
| `t_auth_user_role` | `t_auth_user_role_mapping` | 用户角色表不匹配 |
| `t_auth_role_permission` | `t_auth_role_permission_mapping` | 角色权限表不匹配 |
| `t_auth_role.app_code,data_scope,status` | `RolePO.role_type,build_in,remark` | 角色字段不匹配 |
| `t_auth_permission.app_code,status` | `PermissionPO.identity_lineage,remark` | 权限字段不匹配 |

这意味着当前角色、权限、用户接口即使逻辑能跑，也不是基于 V2 模型在运行。

### 4.2 `app_code` 没有进入接口与鉴权链路

V2 中最重要的隔离键是 `app_code`，但当前代码中：

- 登录接口没有显式携带 `appCode`
- `RoleService` / `PermissionService` 查询未按 `app_code` 过滤
- `StpInterfaceImpl` 的角色、权限缓存未纳入 `app_code`
- `t_auth_user_app` 没有任何接口和服务落地

结果是：多应用隔离能力在数据库层存在，在接口层与鉴权层不存在。

### 4.3 B/C 登录态未真正隔离

虽然仓库新增了 `StpMemberUtil`，但仍存在以下问题：

- `AuthMemberController` 仍在使用 `StpUtil`
- `StpInterfaceImpl` 的缓存 key 只按 `loginId`，没有纳入 `loginType`
- 如果 `sysUser.id = 1` 且 `member.id = 1`，角色/权限缓存可能串用

建议：所有鉴权缓存 key 必须至少使用 `loginType + appCode + loginId`。

### 4.4 网关公开接口白名单未更新

当前网关放行的是旧路径：

- `/apis/v1/auth/login`
- `/apis/v1/auth/register`
- `/apis/v1/auth/isLogin`

但当前 Controller 已改为：

- `/apis/v1/auth/s/login`
- `/apis/v1/auth/m/*`

结果是：新 B/C 登录接口与网关白名单不一致，登录开放路径配置失真。

### 4.5 注解和权限码不完整

当前至少存在以下控制点缺失：

- `UserSysController.saveUser` 缺少 `user:add`
- `RoleController.getMyRoles` 使用了空的 `@SaCheckPermission`
- `PermissionController.import/export/progress` 缺少权限控制
- `UserMemberController` 直接复用 B 端 `user:*` 权限码

### 4.6 C 端权限模型缺失

如果 C 端只做会员账号体系，那么应设计成“轻 RBAC + 场景能力权限”；
如果 C 端也要支持分级会员、分销员、商家、店长、客服等角色，那么当前 V2 至少缺少以下补充之一：

方案 A：

- 给 `t_auth_user_role`、`t_auth_user_app` 增加 `principal_type`

方案 B：

- 新增 `t_member_user_role`
- 新增 `t_member_user_app`

在“B/C 接口完全分离”的要求下，更推荐方案 B，因为领域边界更清晰。

## 5. 建议的接口分层方案

## 5.1 总体约定

为减少改造成本，建议保留当前 `s/m` 语义，但把所有通用接口都显式拆成 B/C 两套路由：

- B 端前缀：`/apis/v1/**/s`
- C 端前缀：`/apis/v1/**/m`

统一约定：

- B 端只允许 `StpUtil`
- C 端只允许 `StpMemberUtil`
- 所有权限码都必须携带应用语义
- 所有登录、鉴权、菜单、接口权限都要显式带上 `appCode`

推荐权限码命名：

- B 端：`sys:user:add`、`sys:role:view`
- C 端：`mall:member:view`、`mall:order:create`

## 5.2 B 端完整接口方案

### 5.2.1 认证接口

| 方法 | 路径 | 功能 | 关键参数 | 访问控制 |
| --- | --- | --- | --- | --- |
| POST | `/apis/v1/auth/s/login` | B 端登录 | `appCode, username, password, device` | 公开；校验 `t_sso_app.user_type = sys` 与 `t_auth_user_app` |
| POST | `/apis/v1/auth/s/logout` | 退出 | 无 | `sys-login` |
| GET | `/apis/v1/auth/s/me` | 获取当前登录用户 | 无 | `sys-login` |
| GET | `/apis/v1/auth/s/permissions` | 获取当前权限码集合 | `appCode` | `sys-login` |
| GET | `/apis/v1/auth/s/routes` | 获取当前菜单/路由树 | `appCode` | `sys-login` |
| POST | `/apis/v1/auth/s/user/update/password` | 修改本人密码 | `oldPassword, newPassword` | `sys-login` |
| POST | `/apis/v1/auth/s/user/reset-password/{userId}` | 重置他人密码 | `userId` | `sys:user:reset` |
| POST | `/apis/v1/auth/s/forgot-password` | 忘记密码 | `username, code, newPassword` | 公开 |

### 5.2.2 用户与组织接口

| 方法 | 路径 | 功能 | 关键参数 | 访问控制 |
| --- | --- | --- | --- | --- |
| POST | `/apis/v1/user/s` | 新增后台用户 | `username, phoneNumber, deptId, postIds, roleIds, appCodes` | `sys:user:add` |
| PUT | `/apis/v1/user/s/{userId}` | 更新后台用户 | `nickname, avatar, deptId, status` | `sys:user:edit` |
| GET | `/apis/v1/user/s/{userId}` | 用户详情 | `userId` | `sys:user:view` |
| POST | `/apis/v1/user/s/page` | 用户分页查询 | `pageNum, pageSize, deptId, username, status` | `sys:user:list` + 数据权限 |
| PATCH | `/apis/v1/user/s/{userId}/status` | 启停用户 | `status` | `sys:user:disable` |
| DELETE | `/apis/v1/user/s/{userId}` | 删除用户 | `userId` | `sys:user:remove` |
| PUT | `/apis/v1/user/s/{userId}/roles` | 用户绑定角色 | `roleIds[]` | `sys:user:bind-role` |
| PUT | `/apis/v1/user/s/{userId}/posts` | 用户绑定岗位 | `postIds[]` | `sys:user:bind-post` |
| PUT | `/apis/v1/user/s/{userId}/apps` | 用户授权应用 | `appCodes[]` | `sys:user:bind-app` |
| GET | `/apis/v1/dept/s/tree` | 部门树 | `status` | `sys:dept:list` |
| POST | `/apis/v1/dept/s` | 新增部门 | `parentId, deptName, sortNum` | `sys:dept:add` |
| PUT | `/apis/v1/dept/s/{deptId}` | 修改部门 | `deptName, status` | `sys:dept:edit` |
| DELETE | `/apis/v1/dept/s/{deptId}` | 删除部门 | `deptId` | `sys:dept:remove` |
| POST | `/apis/v1/post/s/page` | 岗位分页 | `postCode, postName, status` | `sys:post:list` |
| POST | `/apis/v1/post/s` | 新增岗位 | `postCode, postName, sortNum` | `sys:post:add` |
| PUT | `/apis/v1/post/s/{postId}` | 修改岗位 | `postName, status` | `sys:post:edit` |
| DELETE | `/apis/v1/post/s/{postId}` | 删除岗位 | `postId` | `sys:post:remove` |

### 5.2.3 角色、权限、数据权限接口

| 方法 | 路径 | 功能 | 关键参数 | 访问控制 |
| --- | --- | --- | --- | --- |
| POST | `/apis/v1/role/s` | 新增角色 | `appCode, roleName, roleCode, dataScope, remark` | `sys:role:add` |
| PUT | `/apis/v1/role/s/{roleId}` | 修改角色 | 同上 | `sys:role:edit` |
| GET | `/apis/v1/role/s/{roleId}` | 角色详情 | `roleId` | `sys:role:view` |
| POST | `/apis/v1/role/s/page` | 角色分页 | `appCode, searchKey, status` | `sys:role:list` |
| DELETE | `/apis/v1/role/s/{roleId}` | 删除角色 | `roleId` | `sys:role:remove` |
| PUT | `/apis/v1/role/s/{roleId}/permissions` | 角色授权权限 | `permissionIds[]` | `sys:role:assign-permission` |
| PUT | `/apis/v1/role/s/{roleId}/users` | 角色授权用户 | `userIds[]` | `sys:role:bind-user` |
| PUT | `/apis/v1/role/s/{roleId}/data-scope` | 角色数据范围配置 | `dataScope, deptIds[]` | `sys:role:data-scope` |
| POST | `/apis/v1/permission/s` | 新增权限 | `appCode, name, identification, type, parentId, path` | `sys:permission:add` |
| PUT | `/apis/v1/permission/s/{permissionId}` | 修改权限 | 同上 | `sys:permission:edit` |
| DELETE | `/apis/v1/permission/s/{permissionId}` | 删除权限 | `permissionId` | `sys:permission:remove` |
| GET | `/apis/v1/permission/s/tree` | 权限树 | `appCode, searchKey` | `sys:permission:list` |
| POST | `/apis/v1/permission/s/import` | 导入权限 | `file, appCode` | `sys:permission:import` |
| GET | `/apis/v1/permission/s/import/{taskId}` | 查询导入进度 | `taskId` | `sys:permission:import` |
| POST | `/apis/v1/permission/s/export` | 导出权限 | `appCode` | `sys:permission:export` |
| GET | `/apis/v1/permission/s/export/{taskId}` | 查询导出进度 | `taskId` | `sys:permission:export` |

### 5.2.4 应用接口

| 方法 | 路径 | 功能 | 关键参数 | 访问控制 |
| --- | --- | --- | --- | --- |
| POST | `/apis/v1/app/s` | 新增应用 | `appCode, appName, userType, status` | `sys:app:add` |
| PUT | `/apis/v1/app/s/{appCode}` | 修改应用 | `appName, status` | `sys:app:edit` |
| POST | `/apis/v1/app/s/page` | 应用分页 | `appCode, userType, status` | `sys:app:list` |
| GET | `/apis/v1/app/s/{appCode}` | 应用详情 | `appCode` | `sys:app:view` |
| DELETE | `/apis/v1/app/s/{appCode}` | 删除应用 | `appCode` | `sys:app:remove` |

## 5.3 C 端完整接口方案

### 5.3.1 先补齐数据模型

为了保证 C 端权限体系独立，推荐在 V2 基础上补充以下表之一：

推荐方案：

- `t_member_user_role`
- `t_member_user_app`

如果希望继续共用原映射表，则必须把以下表升级为“带主体类型”的通用映射：

- `t_auth_user_role(principal_type, principal_id, role_id, ...)`
- `t_auth_user_app(principal_type, principal_id, app_code, ...)`

### 5.3.2 会员认证与自服务接口

| 方法 | 路径 | 功能 | 关键参数 | 访问控制 |
| --- | --- | --- | --- | --- |
| POST | `/apis/v1/auth/m/login/password` | 会员密码登录 | `appCode, phoneNumber, password, device` | 公开；校验 `t_sso_app.user_type = member` |
| POST | `/apis/v1/auth/m/login/sms` | 会员验证码登录 | `appCode, phoneNumber, code, device` | 公开 |
| POST | `/apis/v1/auth/m/login/wechat` | 微信登录 | `appCode, code` | 公开 |
| POST | `/apis/v1/auth/m/register` | 会员注册 | `appCode, phoneNumber, password, code` | 公开 |
| POST | `/apis/v1/auth/m/send-code` | 发送验证码 | `scene, phoneNumber` | 公开；限流 |
| POST | `/apis/v1/auth/m/logout` | 会员退出 | 无 | `member-login` |
| GET | `/apis/v1/auth/m/me` | 当前会员信息 | 无 | `member-login` |
| GET | `/apis/v1/auth/m/permissions` | 当前会员权限码 | `appCode` | `member-login` |
| GET | `/apis/v1/auth/m/routes` | 当前会员路由/菜单 | `appCode` | `member-login` |
| POST | `/apis/v1/auth/m/password` | 修改本人密码 | `oldPassword, newPassword` | `member-login` |
| POST | `/apis/v1/auth/m/forgot-password` | 忘记密码 | `phoneNumber, code, newPassword` | 公开 |
| GET | `/apis/v1/member/m/profile` | 会员资料 | 无 | `member-login` |
| PUT | `/apis/v1/member/m/profile` | 更新资料 | `nickname, avatar, email` | `member-login` |
| DELETE | `/apis/v1/member/m/cancel` | 注销账号 | `password/code` | `member-login` |

### 5.3.3 C 端权限管理接口

如果 C 端不仅仅是普通会员，还存在分销员、店长、商家、客服、VIP 等差异化能力，则必须补齐独立权限管理接口：

| 方法 | 路径 | 功能 | 关键参数 | 访问控制 |
| --- | --- | --- | --- | --- |
| POST | `/apis/v1/role/m` | 新增会员角色 | `appCode, roleName, roleCode, remark` | `mall:role:add` |
| PUT | `/apis/v1/role/m/{roleId}` | 修改会员角色 | 同上 | `mall:role:edit` |
| GET | `/apis/v1/role/m/{roleId}` | 会员角色详情 | `roleId` | `mall:role:view` |
| POST | `/apis/v1/role/m/page` | 会员角色分页 | `appCode, searchKey` | `mall:role:list` |
| DELETE | `/apis/v1/role/m/{roleId}` | 删除会员角色 | `roleId` | `mall:role:remove` |
| PUT | `/apis/v1/role/m/{roleId}/permissions` | 分配会员权限 | `permissionIds[]` | `mall:role:assign-permission` |
| PUT | `/apis/v1/member/m/{memberId}/roles` | 分配会员角色 | `roleIds[]` | `mall:member:bind-role` |
| POST | `/apis/v1/permission/m` | 新增会员权限 | `appCode, name, identification, type, parentId` | `mall:permission:add` |
| PUT | `/apis/v1/permission/m/{permissionId}` | 修改会员权限 | 同上 | `mall:permission:edit` |
| DELETE | `/apis/v1/permission/m/{permissionId}` | 删除会员权限 | `permissionId` | `mall:permission:remove` |
| GET | `/apis/v1/permission/m/tree` | 会员权限树 | `appCode, searchKey` | `mall:permission:list` |

说明：

- 这里的 `/role/m` 与 `/permission/m` 不是给普通会员自助调用，而是给“电商业务线管理员”或“商城运营后台”使用。
- 普通会员只调用 `/auth/m/*` 与 `/member/m/*`。

## 6. 建议补充的权限粒度

### 6.1 B 端

建议至少补齐以下权限码：

- `sys:user:add`
- `sys:user:view`
- `sys:user:edit`
- `sys:user:remove`
- `sys:user:disable`
- `sys:user:reset`
- `sys:user:bind-role`
- `sys:user:bind-post`
- `sys:user:bind-app`
- `sys:dept:*`
- `sys:post:*`
- `sys:role:*`
- `sys:role:data-scope`
- `sys:permission:*`
- `sys:app:*`

### 6.2 C 端

建议至少补齐以下权限码：

- `mall:member:view`
- `mall:member:edit`
- `mall:member:disable`
- `mall:member:bind-role`
- `mall:role:*`
- `mall:permission:*`
- `mall:order:view`
- `mall:order:create`
- `mall:coupon:claim`
- `mall:address:edit`

## 7. 建议的访问控制规则

### 7.1 B 端

- 登录成功后必须同时满足：账号可用、应用可访问、角色可用、权限可用。
- 用户查询类接口必须叠加 `data_scope`。
- 角色/权限查询必须强制按 `appCode` 过滤。
- 所有 B 端缓存键必须使用 `sys:{appCode}:{loginId}`。

### 7.2 C 端

- 登录成功后必须同时满足：会员状态正常、目标应用属于 `member` 类型。
- 普通会员默认仅能访问“本人数据”。
- 如引入会员角色，则角色、权限、菜单树也必须按 `appCode` 过滤。
- 所有 C 端缓存键必须使用 `member:{appCode}:{loginId}`。

## 8. 实施优先级建议

### P0：先修正基础链路

1. 统一以 V2 为准，重建实体映射和 Mapper。
2. 修正网关放行路径，只放行真实的 B/C 登录注册接口。
3. 拆分 `AuthService` / `UserService` 为 `Sys*` 和 `Member*` 两套服务。
4. 修正 `StpInterfaceImpl` 缓存键，纳入 `loginType + appCode + loginId`。
5. 修正 `PermissionCacheCleanListener` 的 Redis key 前缀不一致问题。

### P1：补齐 B 端缺失接口

1. `me / permissions / routes`
2. 部门、岗位、应用、用户应用授权
3. 角色数据范围配置
4. 导入导出鉴权

### P2：补齐 C 端完整闭环

1. 注册、验证码登录、密码登录、忘记密码
2. 会员资料维护、自助注销
3. 会员角色与权限模型
4. C 端菜单/按钮/接口权限返回

## 9. 最终建议

当前仓库不适合继续基于现有 Controller 直接扩写权限接口，因为：

- 代码依赖 V1，数据库目标是 V2
- B/C 只分了路由前缀，未分主体服务
- C 端权限模型还没补完

建议按以下顺序推进：

1. 先以 V2 为准修正实体和映射表。
2. 把 B/C 服务链、登录态、缓存、网关白名单彻底拆开。
3. 先补 B 端完整 RBAC，再补 C 端会员权限模型。
4. 最后统一提供 `/me / permissions / routes` 三类前端装配接口。
