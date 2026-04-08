# 后台管理系统接口文档

> 生成基准：基于当前项目 `F:\zhanglx-index\zhanglx-sso-platform` 中真实 Controller、DTO/VO、Service、统一返回体、分页对象、异常处理、Sa-Token 鉴权配置、数据库脚本整理。  
> 适用对象：后台管理系统前端联调。  
> 扫描时间：2026-04-06。

## 0. 通用说明

### 0.1 服务前缀与路由归类

建议前端统一通过网关访问，网关默认端口见 `sso-gateway`，后端认证服务为 `sso-auth`。

| 路由前缀 | 业务归类 | 说明 |
| --- | --- | --- |
| `/apis/v1/auth/s/**` | B 端后台管理 / 系统用户 | 后台登录、用户、角色、权限、部门、岗位、应用、参数、字典等 |
| `/apis/v1/auth/s/bindings/**` | B 端绑定关系 | 用户-应用、用户-岗位、角色-部门 |
| `/apis/v1/auth/m/**` | C 端会员认证 | 会员登录、注册、验证码、微信登录、密码修改、找回密码 |
| `/apis/v1/auth/m/users/**` | C 端会员个人中心 | 当前会员信息、绑手机、注销 |

补充说明：

- 当前代码中没有单独的 `/apis/v1/auth/...` 公共后台接口分组。
- 当前代码中没有独立的“菜单管理 Controller”。菜单、按钮、接口权限统一建模在权限模块里，通过 `PermissionDTO.type` 区分。
- 当前代码中没有后台管理型 C 端会员列表/详情/禁用接口，只有会员自助认证和个人资料接口。

### 0.2 鉴权方式

项目使用 Sa-Token。

| 项 | 说明 |
| --- | --- |
| Header | `token: {tokenValue}` |
| Cookie | 也支持从 Cookie 读取 token |
| B 端 token | 默认登录体系 |
| C 端 token | 使用独立 loginType `member`，与 B 端 token 物理隔离 |
| Same-Token | 仅网关与下游服务内部调用使用，前端不需要传 |

前端联调建议：

- 所有需要登录的接口，统一带 `token` 请求头。
- B 端与 C 端虽然 header 名相同，但 token 不通用。
- 一般以网关域名访问接口，不要直接绕过网关调用微服务。

### 0.3 Content-Type 与请求风格

| 类型 | 说明 |
| --- | --- |
| JSON Body 接口 | `Content-Type: application/json` |
| GET 查询接口 | 使用 Query / Path 参数 |
| 批量删除 | 多数批量删除接口也是 JSON Body，Body 直接传数组 |
| 分页查询 | 统一使用 `POST /page` + JSON Body |

### 0.4 统一返回体

所有 Controller 返回值都会被全局包装为统一结构：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `code` | `number` | 业务状态码；成功一般为 `200` |
| `msg` | `string` | 提示信息 |
| `data` | `any` | 业务数据；无返回内容时为 `null` |

注意：

- Controller 返回 `void` 时，响应仍会被包装，最终是 `data: null`。
- 错误响应时，HTTP Status 会尽量与 `code` 对齐，例如 `401/403/404/409/500`。

### 0.5 通用错误响应

```json
{
  "code": 400,
  "msg": "参数错误: 账号不能为空",
  "data": null
}
```

常见状态码：

| code | 含义 | 常见场景 |
| --- | --- | --- |
| `200` | 成功 | 查询/保存成功 |
| `400` | 参数错误 | 校验失败、ID 非法、缺参数 |
| `401` | 未登录 | `SaCheckLogin` 未通过 |
| `403` | 无权限 | `SaCheckPermission` / `SaCheckRole` 未通过 |
| `404` | 资源不存在 | 详情 ID 不存在、按 key 查不到 |
| `405` | 方法不允许 | 请求方式不匹配 |
| `409` | 数据冲突 | 唯一键冲突、手机号已存在、角色编码已存在 |
| `500` | 系统繁忙/内部错误 | 未捕获异常 |

常见业务错误消息：

| 业务 | 典型消息 |
| --- | --- |
| 用户 | `账号已存在`、`账号已被禁用`、`原密码错误` |
| 角色 | `角色编码已存在`、`角色名称已存在` |
| 应用 | `应用编码已存在`、`应用名称已存在` |
| 部门 | `同级部门名称已存在`、`存在下级部门无法删除` |
| 字典 | `字典类型已存在`、`同一字典类型下字典值已存在` |
| 会员 | `会员不存在`、`手机号已被绑定`、`验证码过于频繁` |

### 0.6 雪花 ID 传输规范

这是本项目最重要的前后端约定之一：

- Java / DB 中主键为 `Long`
- 全局 Jackson 已将 `Long` 序列化为 `String`
- 全局 Jackson 也允许前端把字符串形式的 ID 反序列化回 `Long`

前端必须统一按以下规则处理：

1. 所有 ID 一律按 `string` 使用和存储。
2. 所有 Path 参数 ID 一律传字符串数字，例如 `"/users/2031984412698099713"`。
3. 所有 Body 中的 ID 一律传字符串，例如：

```json
{
  "deptId": "1100000000000000001"
}
```

4. 所有批量 ID 一律用 `string[]`，例如：

```json
["2031984412698099713", "2031984412698099714"]
```

5. 不要把任何 ID 当成 JavaScript `number` 使用，避免精度丢失。

受此规则影响的典型字段：

`id`、`userId`、`roleId`、`permissionId`、`parentId`、`deptId`、`postId`、`configId`、`ids`、`userIds`、`deptIds`、`postIds`

补充说明：

- `RequestIdUtils` 对 Path ID 的要求是：必须为正整数、不能为空、不能小于等于 0。
- 绑定“应用”时是个例外：接口用的是 `appCode: string`，不是 `appId`。

### 0.7 时间格式

| 类型 | 格式 | 说明 |
| --- | --- | --- |
| `LocalDateTime` | `yyyy-MM-dd HH:mm:ss` | 例如 `2026-04-06 21:30:00` |
| `LocalDate` | `yyyy-MM-dd` | 例如 `2026-04-06` |

注意：

- 项目已全局配置 `LocalDateTime` 的字符串序列化/反序列化。
- 传时间时不要传时间戳，直接传字符串。

### 0.8 分页请求与返回

分页请求基类是 `PageQuery`：

| 字段 | 前端类型 | 是否必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `pageNum` | `number` | 否 | `1` | 页码，从 1 开始 |
| `pageSize` | `number` | 否 | `10` | 每页条数，最大 `100` |
| `searchKey` | `string` | 否 | `""` | 通用模糊/关键字查询 |
| `sortingFields` | `{ field: string; order: "asc" \| "desc" }[]` | 否 | `[]` | 排序字段；当前大多数接口未实际使用 |

分页返回以 MyBatis-Plus `Page<T>` 为主，前端可优先使用这些字段：

| 字段 | 前端类型 | 说明 |
| --- | --- | --- |
| `records` | `T[]` | 当前页数据 |
| `total` | `number` | 总条数 |
| `current` | `number` | 当前页码 |
| `size` | `number` | 每页条数 |

分页返回示例：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "records": [],
    "total": 0,
    "current": 1,
    "size": 10
  }
}
```

### 0.9 通用状态与枚举

| 字段 | 值 | 含义 |
| --- | --- | --- |
| `status` | `1` | 启用 / 正常 |
| `status` | `0` | 停用 / 禁用 |
| `sex` | `0` | 未知 |
| `sex` | `1` | 男 |
| `sex` | `2` | 女 |
| `allowConcurrentLogin` | `1` | 允许并发登录 |
| `allowConcurrentLogin` | `0` | 不允许并发登录，新登录会顶掉旧登录 |
| `app.userType` | `1` | 系统用户应用 |
| `app.userType` | `2` | 会员用户应用 |
| `role.dataScope` | `1` | 全部数据 |
| `role.dataScope` | `2` | 本部门及以下 |
| `role.dataScope` | `3` | 本部门 |
| `role.dataScope` | `4` | 本人 |
| `role.dataScope` | `5` | 自定义数据范围 |
| `permission.type` | `-1` | 平台 |
| `permission.type` | `0` | 模块 |
| `permission.type` | `1` | 菜单 |
| `permission.type` | `2` | 按钮 |
| `permission.type` | `3` | 接口 |
| `permission.isFrame` | `0` | 非外链 |
| `permission.isFrame` | `1` | 外链 |
| `configType` | `1` | 系统内置 |
| `configType` | `0` | 普通配置 |

### 0.10 基础审计字段

多数 DTO/VO 继承 `BaseDTO` / `BaseVO`，会带以下只读字段：

| 字段 | 前端类型 | 说明 |
| --- | --- | --- |
| `id` | `string` | 主键 |
| `createBy` | `string` | 创建人 ID |
| `createTime` | `string` | 创建时间 |
| `updateBy` | `string` | 更新人 ID |
| `updateTime` | `string` | 更新时间 |

这些字段：

- 列表页/详情页可展示
- 新增/编辑提交时一般不要主动传
- 前端本地状态里一律按字符串保存 ID

## 1. B 端用户管理

### 1.1 用户对象说明

#### 1.1.1 `UserDTO` / `UserBaseDTO`

| 字段 | 前端类型 | 新增 | 编辑 | 列表/详情返回 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `id` | `string` | 否 | 否 | 是 | 用户 ID |
| `username` | `string` | 是 | 否 | 是 | 登录账号；新增必填；编辑接口不支持修改 |
| `nickname` | `string` | 否 | 否 | 是 | 昵称/用户名称 |
| `avatar` | `string` | 否 | 否 | 是 | 头像 URL |
| `phoneNumber` | `string` | 否 | 否 | 是 | 手机号；全局唯一 |
| `sex` | `number` | 否 | 否 | 是 | `0` 未知，`1` 男，`2` 女 |
| `birthday` | `string` | 否 | 否 | DTO 有字段 | 当前代码未实际落库，前端不要依赖 |
| `email` | `string` | 否 | 否 | 是 | 邮箱 |
| `allowConcurrentLogin` | `number` | 否 | 否 | 是 | `1` 允许并发，`0` 不允许并发 |
| `deptId` | `string` | 否 | 否 | 是 | 所属部门 ID；虽然 Swagger 标注为只读，但代码实际支持提交 |
| `deptName` | `string` | 否 | 否 | 是 | 部门名称，仅展示 |
| `status` | `number` | 否 | 否 | 是 | `1` 正常，`0` 禁用；编辑走独立状态接口 |

新增与编辑差异：

- 新增用 `UserDTO`
- 编辑用 `UserBaseDTO`
- 编辑接口不能改 `username`
- 状态修改走独立接口 `PATCH /{userId}/status`
- 密码不在新增/编辑 body 中提交，新增后端直接使用默认初始密码

#### 1.1.2 `UserPageQueryDTO`

| 字段 | 前端类型 | 是否必填 | 说明 |
| --- | --- | --- | --- |
| `pageNum` | `number` | 否 | 页码 |
| `pageSize` | `number` | 否 | 每页条数 |
| `searchKey` | `string` | 否 | 关键字，匹配 `username / phoneNumber / nickname` |
| `username` | `string` | 否 | 按账号模糊查询 |
| `deptId` | `string` | 否 | 按部门过滤 |

注意：

- 当前代码没有提供按 `status` 过滤用户列表的能力。

### 1.2 用户列表查询

- 用途：后台用户列表页、用户选择弹窗的数据源。
- 请求：`POST /apis/v1/auth/s/users/page`
- 鉴权：是
- 权限：`user:list`
- Content-Type：`application/json`
- 分页接口：是

请求示例：

```json
{
  "pageNum": 1,
  "pageSize": 10,
  "searchKey": "zhang",
  "username": "zhang",
  "deptId": "1100000000000000001"
}
```

返回示例：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "records": [
      {
        "id": "1300000000000000001",
        "username": "zhanglx",
        "nickname": "超级管理员",
        "avatar": null,
        "phoneNumber": "13800138000",
        "sex": 1,
        "birthday": null,
        "email": "admin@zhanglx.com",
        "allowConcurrentLogin": 1,
        "deptId": "1100000000000000001",
        "deptName": "总公司",
        "status": 1,
        "createBy": "1300000000000000001",
        "createTime": "2026-03-12 14:43:31",
        "updateBy": "1300000000000000001",
        "updateTime": "2026-03-12 14:43:31"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10
  }
}
```

前端对接说明：

- 列表常用列：`username`、`nickname`、`phoneNumber`、`deptName`、`status`、`createTime`
- 搜索框建议绑定 `searchKey`
- 精确筛选可再单独传 `username`、`deptId`
- `status` 适合表格开关，但变更要调用独立接口
- 表格行主键、选中值都按 `string` 保存

### 1.3 用户详情

- 用途：详情抽屉、编辑回显。
- 请求：`GET /apis/v1/auth/s/users/{userId}`
- 鉴权：是
- 权限：`user:view`

Path 参数：

| 字段 | 前端类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `userId` | `string` | 是 | 用户 ID |

返回：`Result<UserDTO>`

请求示例：

```http
GET /apis/v1/auth/s/users/1300000000000000001
token: xxxxx
```

前端对接说明：

- 编辑页建议先调详情接口，再把可编辑字段映射到表单
- `deptName` 只展示不提交
- `birthday` 字段当前不要作为核心业务字段使用

### 1.4 新增用户

- 用途：新增用户弹窗/页面。
- 请求：`POST /apis/v1/auth/s/users`
- 鉴权：是
- 权限：`user:add`
- Content-Type：`application/json`

请求参数（Body）：

| 字段 | 前端类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `username` | `string` | 是 | 登录账号，唯一 |
| `nickname` | `string` | 否 | 昵称 |
| `avatar` | `string` | 否 | 头像 URL |
| `phoneNumber` | `string` | 否 | 手机号，唯一 |
| `sex` | `number` | 否 | `0/1/2` |
| `birthday` | `string` | 否 | DTO 有字段，但当前不建议依赖 |
| `email` | `string` | 否 | 邮箱 |
| `allowConcurrentLogin` | `number` | 否 | 默认 `1` |
| `deptId` | `string` | 否 | 所属部门 ID，部门必须存在且启用 |
| `status` | `number` | 否 | 默认 `1` |

请求示例：

```json
{
  "username": "ops_admin",
  "nickname": "运维管理员",
  "phoneNumber": "13900001111",
  "sex": 1,
  "email": "ops@example.com",
  "allowConcurrentLogin": 1,
  "deptId": "1100000000000000002",
  "status": 1
}
```

返回示例：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

前端对接说明：

- 当前新增接口不返回新对象，保存成功后如需刷新详情，请重新调列表或详情接口
- 当前新增接口不接收密码，后端会使用系统默认密码初始化
- 默认密码来自配置 `default.password`，当前项目默认值为 `123456`
- `deptId` 必须是启用状态部门

注意事项：

- `username` 唯一
- `phoneNumber` 若传则唯一
- 若所属部门已停用，新增会失败

### 1.5 编辑用户

- 用途：编辑用户弹窗/页面保存。
- 请求：`PUT /apis/v1/auth/s/users/{userId}`
- 鉴权：是
- 权限：`user:edit`
- Content-Type：`application/json`

Path 参数：

| 字段 | 前端类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `userId` | `string` | 是 | 用户 ID |

Body 参数：

| 字段 | 前端类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `nickname` | `string` | 否 | 昵称 |
| `avatar` | `string` | 否 | 头像 |
| `phoneNumber` | `string` | 否 | 手机号，唯一 |
| `sex` | `number` | 否 | `0/1/2` |
| `birthday` | `string` | 否 | 当前代码不落库 |
| `email` | `string` | 否 | 邮箱 |
| `allowConcurrentLogin` | `number` | 否 | `0/1` |
| `deptId` | `string` | 否 | 部门 ID |

请求示例：

```json
{
  "nickname": "运维管理员-修改",
  "phoneNumber": "13900002222",
  "sex": 1,
  "email": "ops2@example.com",
  "allowConcurrentLogin": 0,
  "deptId": "1100000000000000002"
}
```

前端对接说明：

- 编辑接口是完整覆盖式更新思路，建议以详情回显后的表单完整提交
- `username` 不在编辑 DTO 中，前端应做只读展示
- `status` 不走本接口

注意事项：

- 修改手机号会做唯一性校验
- 修改到停用部门会失败
- 当前 Service 未处理 `birthday` 持久化，提交该字段不会真正生效

### 1.6 删除用户

- 用途：单个删除。
- 请求：`DELETE /apis/v1/auth/s/users/{userId}`
- 鉴权：是
- 权限：`user:remove`

行为说明：

- 会同步清理用户的第三方账号绑定、应用授权、岗位绑定、角色绑定
- 会强制该用户下线

### 1.7 批量删除用户

- 用途：表格批量删除。
- 请求：`DELETE /apis/v1/auth/s/users`
- 鉴权：是
- 权限：`user:batch-remove`
- Content-Type：`application/json`

Body 参数：

```json
["1300000000000000002", "1300000000000000003"]
```

前端对接说明：

- Body 直接传 `string[]`
- 空数组会报错，不要提交空列表

### 1.8 启用/禁用用户

- 用途：表格状态开关。
- 请求：`PATCH /apis/v1/auth/s/users/{userId}/status`
- 鉴权：是
- 权限：`user:disable`
- Content-Type：`application/json`

Body：

```json
{
  "status": 0
}
```

前端对接说明：

- `status=1` 正常，`status=0` 禁用
- 非常适合做 `switch`
- 若改成禁用，后端会立即强制该用户下线

### 1.9 重置密码

- 用途：管理员重置指定用户密码。
- 请求：`POST /apis/v1/auth/s/user/reset-password/{userId}`
- 鉴权：是
- 权限：`user:reset`

返回：`Result<Void>`

前端对接说明：

- 该接口无 body
- 重置后用户会被强制下线
- 重置后的密码为系统默认密码，当前项目默认 `123456`

### 1.10 当前缺失能力

当前代码中没有提供以下后台用户管理接口：

- 任意用户的“查询已绑定角色”
- 任意用户的“修改绑定角色”
- 管理端“会员列表 / 会员详情 / 会员禁用”
- 用户导入/导出

如果前端页面需要这些能力，需要后端补充接口。

## 2. 用户应用 / 岗位绑定

> 这组接口都在 `/apis/v1/auth/s/bindings/**` 下。  
> 典型用法：用户列表页点“分配应用”“分配岗位”弹窗。

### 2.1 查询用户已绑定应用

- 请求：`GET /apis/v1/auth/s/bindings/users/{userId}/apps`
- 鉴权：是
- 权限：`user:assign-app`
- 返回：`Result<AppDTO[]>`

前端对接说明：

- 该接口只返回已绑定应用，不返回候选应用全集
- 候选应用列表请调用应用分页接口 `POST /apis/v1/auth/s/apps/page`
- 绑定接口使用的是 `appCode`，不是 `appId`

### 2.2 绑定用户应用

- 请求：`PUT /apis/v1/auth/s/bindings/users/{userId}/apps`
- 鉴权：是
- 权限：`user:assign-app`
- Content-Type：`application/json`

Body：

```json
["sso", "mall"]
```

参数说明：

| 字段 | 前端类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| body 数组项 | `string` | 是 | `appCode`，不是应用 ID |

前端对接说明：

- 多选框 `value` 请绑定 `appCode`
- 如需展示名称，`label` 用 `appName`
- 传空数组表示清空用户应用授权
- 只能绑定启用中的系统用户应用，即 `userType=1`

注意事项：

- 这个接口的 body 与其他绑定接口不同，用的是 `appCode[]`
- 停用应用或会员应用不能绑定到后台用户

### 2.3 查询用户已绑定岗位

- 请求：`GET /apis/v1/auth/s/bindings/users/{userId}/posts`
- 鉴权：是
- 权限：`user:assign-post`
- 返回：`Result<PostDTO[]>`

前端对接说明：

- 候选岗位列表请调用岗位分页接口
- 该接口适合编辑弹窗回显已勾选项

### 2.4 绑定用户岗位

- 请求：`PUT /apis/v1/auth/s/bindings/users/{userId}/posts`
- 鉴权：是
- 权限：`user:assign-post`
- Content-Type：`application/json`

Body：

```json
["1200000000000000001", "1200000000000000002"]
```

前端对接说明：

- 多选值用 `post.id`，按 `string` 处理
- 传空数组表示清空岗位
- 只能绑定启用中的岗位

## 3. 角色管理

### 3.1 角色对象说明

#### 3.1.1 `RoleDTO`

| 字段 | 前端类型 | 新增/编辑 | 返回 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | `string` | 否 | 是 | 角色 ID |
| `roleName` | `string` | 是 | 是 | 角色名称 |
| `roleCode` | `string` | 是 | 是 | 角色编码；同一应用下唯一 |
| `appCode` | `string` | 否 | 是 | 所属应用编码；为空时后端默认 `sso` |
| `dataScope` | `number` | 否 | 是 | `1全部 / 2本部门及以下 / 3本部门 / 4本人 / 5自定义` |
| `status` | `number` | 否 | 是 | `1启用 / 0停用` |
| `remark` | `string` | 否 | 是 | 备注 |
| `rolePermissions` | `PermissionVO[]` | 否 | 详情返回 | 角色已绑定权限列表 |
| `roleType` | `string` | 否 | 兼容字段 | 当前代码未真实落库，通常可忽略 |
| `buildIn` | `number` | 否 | 兼容字段 | 当前代码未真实落库，通常可忽略 |

#### 3.1.2 `RoleInfoVO`

| 字段 | 前端类型 | 说明 |
| --- | --- | --- |
| `id` | `string` | 角色 ID |
| `roleName` | `string` | 角色名称 |
| `roleCode` | `string` | 角色编码 |
| `appCode` | `string` | 所属应用编码 |
| `dataScope` | `number` | 数据范围 |
| `status` | `number` | 状态 |
| `userIds` | `string[]` | 已绑定用户 ID 列表，仅 `GET /roles/{roleId}/users` 返回可靠 |

#### 3.1.3 `RolePermissionRelationshipMappingDTO`

| 字段 | 前端类型 | 是否建议传 | 说明 |
| --- | --- | --- | --- |
| `permissionId` | `string` | 是 | 权限 ID，真正生效字段 |
| `roleId` | `string` | 否 | 实际以后端 path `roleId` 为准，可不传 |
| `expireTime` | `string` | 否 | 当前 Service 未实际处理，可不传 |

### 3.2 角色列表查询

- 请求：`POST /apis/v1/auth/s/roles/page`
- 鉴权：是
- 权限：`role:list`
- 分页：是

Body：

```json
{
  "pageNum": 1,
  "pageSize": 10,
  "searchKey": "admin"
}
```

前端对接说明：

- 当前角色列表查询只实际支持 `pageNum/pageSize/searchKey`
- `searchKey` 会匹配 `roleCode / roleName / appCode`
- 如果页面上想做“按状态过滤 / 按应用过滤”，当前后端没有单独筛选字段

### 3.3 角色详情

- 请求：`GET /apis/v1/auth/s/roles/{id}`
- 鉴权：是
- 权限：`role:view`
- 返回：`Result<RoleDTO>`

返回重点：

- `rolePermissions` 为该角色当前已绑定的权限明细
- 前端做“分配菜单/权限”弹窗回显时，通常用这个字段拿已选权限 ID

### 3.4 新增角色

- 请求：`POST /apis/v1/auth/s/roles`
- 鉴权：是
- 权限：`role:add`
- Content-Type：`application/json`

请求示例：

```json
{
  "roleName": "运营管理员",
  "roleCode": "OPS_ADMIN",
  "appCode": "sso",
  "dataScope": 5,
  "status": 1,
  "remark": "负责日常运营"
}
```

返回：`Result<RoleDTO>`

注意事项：

- `appCode` 为空时默认 `sso`
- `dataScope` 为空时默认 `1`
- `status` 为空时默认 `1`
- `roleCode`、`roleName` 在同一 `appCode` 下唯一

### 3.5 编辑角色

- 请求：`PUT /apis/v1/auth/s/roles/{id}`
- 鉴权：是
- 权限：`role:edit`
- Content-Type：`application/json`

前端对接说明：

- 编辑接口是全量更新思路，建议带完整表单对象
- 如果把 `dataScope` 改成非 `5`，后端会自动清空该角色已绑定的部门数据范围

### 3.6 删除角色

- 请求：`DELETE /apis/v1/auth/s/roles/{id}`
- 鉴权：是
- 权限：`role:remove`

删除后会一并清理：

- 用户-角色绑定
- 角色-部门绑定
- 角色-权限绑定

### 3.7 批量删除角色

- 请求：`DELETE /apis/v1/auth/s/roles`
- 鉴权：是
- 权限：`role:remove`
- Body：`string[]`

```json
["1400000000000000001", "1400000000000000002"]
```

### 3.8 启用/禁用角色

- 请求：`PATCH /apis/v1/auth/s/roles/{id}/status`
- 鉴权：是
- 权限：`role:status`
- Body：

```json
{
  "status": 0
}
```

### 3.9 查询角色下已绑定用户

- 请求：`GET /apis/v1/auth/s/roles/{roleId}/users`
- 鉴权：是
- 权限：`role:view`
- 返回：`Result<RoleInfoVO>`

前端对接说明：

- 用于“分配用户”弹窗回显
- `userIds` 为已勾选用户 ID 数组
- 候选用户列表请使用用户分页接口

### 3.10 绑定角色用户

- 请求：`PUT /apis/v1/auth/s/roles/{roleId}/users`
- 鉴权：是
- 权限：`role:bind-user`
- Body：`string[]`

```json
["1300000000000000001", "1300000000000000002"]
```

前端对接说明：

- 传空数组表示清空该角色下所有用户
- 值类型一律 `string[]`
- 建议保存成功后，重新调用 `GET /roles/{roleId}/users`

注意事项：

- 当前实现返回的 `RoleInfoVO` 不可靠地携带最新 `userIds`
- 因此前端不要直接信任 PUT 的返回数据做回显

### 3.11 绑定角色菜单 / 权限

- 请求：`PUT /apis/v1/auth/s/roles/{roleId}/permissions`
- 鉴权：是
- 权限：`role:assign-permission`
- Content-Type：`application/json`

Body 示例：

```json
[
  {
    "permissionId": "3000000000000000100"
  },
  {
    "permissionId": "3000000000000001205"
  }
]
```

前端对接说明：

- 本项目没有独立菜单接口，角色绑定菜单 = 角色绑定权限树中的 `type=1` 节点
- 一般可把整棵权限树作为授权树：
  - 菜单：`type=1`
  - 按钮：`type=2`
  - 接口：`type=3`
- 若页面只想绑菜单，可只取 `type=1`
- 若页面想绑完整可操作权限，可保留全部节点
- 保存成功后如需重新回显已授权权限，请再调角色详情接口

注意事项：

- 当前实现真正使用的字段只有 `permissionId`
- `expireTime` 当前未落业务逻辑
- 传空数组表示清空角色全部权限

### 3.12 查询当前登录人角色

- 请求：`GET /apis/v1/auth/s/roles/my`
- 鉴权：是
- 权限：仅登录，不要求额外权限

用途：

- 当前后台用户登录后，如前端要根据角色编码控制页面，可直接使用

## 4. 权限 / 菜单管理

> 本项目“菜单、按钮、接口权限”统一都由权限模块管理，没有 `/menus` Controller。  
> 前端要做菜单树/权限树/路由配置页，直接使用这里的接口。

### 4.1 权限对象说明

#### 4.1.1 `PermissionDTO`

| 字段 | 前端类型 | 新增/编辑 | 返回 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | `string` | 否 | 是 | 权限 ID |
| `name` | `string` | 是 | 是 | 名称 |
| `identification` | `string` | 是 | 是 | 权限标识，唯一 |
| `parentId` | `string` | 是 | 是 | 父节点 ID，根节点可传 `"0"` / `null` |
| `identityLineage` | `string` | 否 | 是 | 标识血缘路径，建议只展示不改 |
| `comPath` | `string` | 否 | 是 | 前端组件路径 |
| `path` | `string` | 否 | 是 | 前端路由路径 |
| `iconStr` | `string` | 否 | 是 | 图标 |
| `displayNo` | `number` | 是 | 是 | 排序号 |
| `isFrame` | `number` | 否 | 是 | `0` 非外链，`1` 外链 |
| `type` | `number` | 是 | 是 | `-1平台 / 0模块 / 1菜单 / 2按钮 / 3接口` |
| `remark` | `string` | 否 | 是 | 备注 |
| `status` | `number` | 否 | 是 | `1启用 / 0停用` |
| `children` | `PermissionDTO[]` | 否 | 树接口返回 | 子节点 |

树渲染建议：

| 场景 | 推荐字段 |
| --- | --- |
| `key` | `id` |
| `label` | `name` |
| `children` | `children` |
| 辅助展示 | `type`、`identification`、`path`、`comPath`、`iconStr`、`status` |

#### 4.1.2 `PermissionQueryDTO`

| 字段 | 前端类型 | 是否必填 | 说明 |
| --- | --- | --- | --- |
| `username` | `string` | 是 | 要查询哪个用户的权限 |
| `identifications` | `string[]` | 否 | 过滤的权限标识列表 |
| `permissionTypes` | `string[]` | 否 | 过滤的权限类型列表，建议传 `"-1" / "0" / "1" / "2" / "3"` |

### 4.2 权限树 / 菜单树查询

- 请求：`GET /apis/v1/auth/s/permissions/tree?searchKey=...`
- 鉴权：是
- 权限：`permission:list`

Query 参数：

| 字段 | 前端类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `searchKey` | `string` | 否 | 当前实现是精确匹配 `name` 或 `identification`，不是模糊包含 |

返回：`Result<PermissionDTO[]>`

前端对接说明：

- 这是权限管理页、角色授权树的核心接口
- 如果要渲染“菜单树”，建议前端在结果里过滤 `type=1`
- 如果要渲染“完整权限树”，直接全量渲染
- 根节点可能表现为 `parentId = "0"` 或 `null`

注意事项：

- 当前树构建逻辑会结合当前登录人的权限集做过滤
- 联调权限管理页时，建议优先使用超管账号
- `searchKey` 不是 contains 模糊搜索，而是等值匹配

### 4.3 权限详情

- 请求：`GET /apis/v1/auth/s/permissions/{id}`
- 鉴权：是
- 权限：`permission:view`

用途：

- 编辑页回显
- 权限详情查看

### 4.4 新增权限 / 菜单

- 请求：`POST /apis/v1/auth/s/permissions`
- 鉴权：是
- 权限：`permission:add`
- Content-Type：`application/json`

请求示例：

```json
{
  "name": "配置中心",
  "identification": "system:auth:config",
  "parentId": "3000000000000000010",
  "comPath": "system/auth/config/index",
  "path": "/system/auth/config",
  "iconStr": "setting",
  "displayNo": 8,
  "isFrame": 0,
  "type": 1,
  "remark": "系统参数菜单",
  "status": 1
}
```

前端对接说明：

- 新增菜单时通常设置 `type=1`
- 新增按钮时一般只关心 `name / identification / parentId / displayNo / type=2`
- 新增接口权限时一般 `type=3`，`path` 可填真实后端路径

注意事项：

- `identification` 全局唯一
- 如果 `parentId` 传了但父节点不存在，当前实现会把该节点降级为根节点，不建议前端依赖这个行为

### 4.5 编辑权限 / 菜单

- 请求：`PUT /apis/v1/auth/s/permissions/{id}`
- 鉴权：是
- 权限：`permission:edit`
- Content-Type：`application/json`

前端对接说明：

- 当前实现更接近“整对象覆盖更新”
- 编辑页请优先：
  1. 先调详情拿完整对象
  2. 用户修改后整体提交
- 不要做只传部分字段的 PATCH 式提交

注意事项：

- 若修改 `identification`，后端会尝试递归更新子节点血缘标识
- `parentId / identityLineage` 变更逻辑较敏感，前端尽量基于详情回显做完整提交

### 4.6 删除权限 / 菜单

- 请求：`DELETE /apis/v1/auth/s/permissions/{id}`
- 鉴权：是
- 权限：`permission:remove`

注意事项：

- 删除会递归删除所有子孙节点
- 删除会一并清理角色-权限关系
- 前端删除前应做二次确认，并提示“会级联删除下级节点”

### 4.7 批量删除权限 / 菜单

- 请求：`DELETE /apis/v1/auth/s/permissions`
- 鉴权：是
- 权限：`permission:remove`
- Body：`string[]`

```json
["3000000000000000100", "3000000000000000101"]
```

### 4.8 启用 / 禁用权限

- 请求：`PATCH /apis/v1/auth/s/permissions/{id}/status`
- 鉴权：是
- 权限：`permission:status`

Body：

```json
{
  "status": 0
}
```

### 4.9 按用户 + 标识 + 类型查询权限

- 请求：`POST /apis/v1/auth/s/permissions/by-identification`
- 鉴权：是
- 权限：`permission:list`
- Content-Type：`application/json`

请求示例：

```json
{
  "username": "zhanglx",
  "identifications": ["system:auth:user", "system:auth:role"],
  "permissionTypes": ["1", "2", "3"]
}
```

返回：`Result<PermissionVO[]>`

适用场景：

- 某个用户的菜单/按钮/接口能力检查
- 权限联调排查
- 前端如果想知道“某用户在某模块下到底有哪些权限点”，可以使用

## 5. 部门 / 组织管理

> 当前项目中对应的是“部门”模型，没有独立机构、租户、组织 Controller。

### 5.1 部门对象说明

#### 5.1.1 `DeptDTO`

| 字段 | 前端类型 | 新增/编辑 | 返回 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | `string` | 否 | 是 | 部门 ID |
| `parentId` | `string` | 否 | 是 | 上级部门 ID；根节点常用 `"0"` |
| `ancestors` | `string` | 否 | 是 | 祖级链，例如 `0,1001,1002`，只展示不提交 |
| `deptName` | `string` | 是 | 是 | 部门名称 |
| `sortNum` | `number` | 否 | 是 | 排序号，默认 `0` |
| `status` | `number` | 否 | 是 | `1启用 / 0停用` |
| `children` | `DeptDTO[]` | 否 | 树接口返回 | 子节点 |

树渲染建议：

| 场景 | 推荐字段 |
| --- | --- |
| `key` | `id` |
| `label` | `deptName` |
| `children` | `children` |

#### 5.1.2 `DeptQueryDTO`

| 字段 | 前端类型 | 说明 |
| --- | --- | --- |
| `pageNum` | `number` | 页码 |
| `pageSize` | `number` | 每页条数 |
| `searchKey` | `string` | 按部门名模糊搜索 |
| `parentId` | `string` | 查某个父节点下的直接子部门 |
| `deptName` | `string` | 按部门名过滤 |
| `status` | `number` | 状态过滤 |

### 5.2 部门分页列表

- 请求：`POST /apis/v1/auth/s/depts/page`
- 鉴权：是
- 权限：`dept:list`

请求示例：

```json
{
  "pageNum": 1,
  "pageSize": 10,
  "searchKey": "华东",
  "parentId": "0",
  "status": 1
}
```

前端对接说明：

- 表格页可用
- 如果是树表联动模式，也可以先查树接口

### 5.3 部门树查询

- 请求：`GET /apis/v1/auth/s/depts/tree`
- 鉴权：是
- 权限：`dept:list`

Query 参数：

| 字段 | 前端类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `deptName` | `string` | 否 | 过滤部门名称 |
| `status` | `number` | 否 | 状态 |

前端对接说明：

- 适合下拉树、树组件、角色数据范围选择
- 若传 `deptName`，后端会返回“命中节点 + 它的祖先链”，便于前端展开定位

### 5.4 部门详情

- 请求：`GET /apis/v1/auth/s/depts/{id}`
- 鉴权：是
- 权限：`dept:view`

### 5.5 新增部门

- 请求：`POST /apis/v1/auth/s/depts`
- 鉴权：是
- 权限：`dept:add`
- Content-Type：`application/json`

请求示例：

```json
{
  "parentId": "1100000000000000001",
  "deptName": "华东运营部",
  "sortNum": 10,
  "status": 1
}
```

注意事项：

- 同一父节点下 `deptName` 唯一
- `parentId` 为空 / `0` 可视为根节点

### 5.6 编辑部门

- 请求：`PUT /apis/v1/auth/s/depts/{id}`
- 鉴权：是
- 权限：`dept:edit`

注意事项：

- 上级部门不能选自己
- 上级部门不能选自己的子节点
- 如果移动了父节点，后端会自动刷新子孙节点 `ancestors`

### 5.7 删除部门

- 请求：`DELETE /apis/v1/auth/s/depts/{id}`
- 鉴权：是
- 权限：`dept:remove`

删除限制：

- 有下级部门时不能删
- 有用户挂在该部门下时不能删
- 已被角色数据范围引用时不能删

### 5.8 启用 / 禁用部门

- 请求：`PATCH /apis/v1/auth/s/depts/{id}/status`
- 鉴权：是
- 权限：`dept:status`

前端对接说明：

- 非常适合开关
- 当禁用部门时，后端会把所有下级部门一并禁用
- 当启用部门时，如果上级部门仍是停用状态，会失败

### 5.9 查询角色已绑定部门（数据范围）

- 请求：`GET /apis/v1/auth/s/bindings/roles/{roleId}/depts`
- 鉴权：是
- 权限：`role:assign-dept`
- 返回：`Result<DeptDTO[]>`

用途：

- 角色数据范围为“自定义”时，回显已选部门

### 5.10 绑定角色部门（数据范围）

- 请求：`PUT /apis/v1/auth/s/bindings/roles/{roleId}/depts`
- 鉴权：是
- 权限：`role:assign-dept`
- Body：`string[]`

```json
["1100000000000000001", "1100000000000000002"]
```

前端对接说明：

- 只有当角色 `dataScope = 5` 时，才允许绑定部门
- 页面交互建议：
  1. 先查角色详情
  2. 若 `dataScope=5`，再调部门树 + 当前已绑定部门
- 传空数组表示清空自定义部门

## 6. 岗位管理

### 6.1 岗位对象说明

#### 6.1.1 `PostDTO`

| 字段 | 前端类型 | 新增/编辑 | 返回 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | `string` | 否 | 是 | 岗位 ID |
| `postCode` | `string` | 是 | 是 | 岗位编码，唯一 |
| `postName` | `string` | 是 | 是 | 岗位名称，唯一 |
| `sortNum` | `number` | 否 | 是 | 排序号，默认 `0` |
| `status` | `number` | 否 | 是 | `1启用 / 0停用` |

#### 6.1.2 `PostQueryDTO`

| 字段 | 前端类型 | 说明 |
| --- | --- | --- |
| `pageNum` | `number` | 页码 |
| `pageSize` | `number` | 每页条数 |
| `searchKey` | `string` | 匹配岗位编码/名称 |
| `postCode` | `string` | 按编码过滤 |
| `postName` | `string` | 按名称过滤 |
| `status` | `number` | 状态 |

### 6.2 岗位分页列表

- 请求：`POST /apis/v1/auth/s/posts/page`
- 鉴权：是
- 权限：`post:list`

### 6.3 岗位详情

- 请求：`GET /apis/v1/auth/s/posts/{id}`
- 鉴权：是
- 权限：`post:view`

### 6.4 新增岗位

- 请求：`POST /apis/v1/auth/s/posts`
- 鉴权：是
- 权限：`post:add`

### 6.5 编辑岗位

- 请求：`PUT /apis/v1/auth/s/posts/{id}`
- 鉴权：是
- 权限：`post:edit`

### 6.6 删除岗位

- 请求：`DELETE /apis/v1/auth/s/posts/{id}`
- 鉴权：是
- 权限：`post:remove`

注意事项：

- 如果仍有用户绑定该岗位，不能删除

### 6.7 批量删除岗位

- 请求：`DELETE /apis/v1/auth/s/posts`
- 鉴权：是
- 权限：`post:remove`
- Body：`string[]`

### 6.8 启用 / 禁用岗位

- 请求：`PATCH /apis/v1/auth/s/posts/{id}/status`
- 鉴权：是
- 权限：`post:status`

## 7. 应用管理

### 7.1 应用对象说明

#### 7.1.1 `AppDTO`

| 字段 | 前端类型 | 新增/编辑 | 返回 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | `string` | 否 | 是 | 应用 ID |
| `appCode` | `string` | 是 | 是 | 应用编码，唯一 |
| `appName` | `string` | 是 | 是 | 应用名称，唯一 |
| `status` | `number` | 否 | 是 | `1启用 / 0停用` |
| `userType` | `number` | 否 | 是 | `1系统用户应用 / 2会员用户应用` |
| `remark` | `string` | 否 | 是 | 备注 |

#### 7.1.2 `AppQueryDTO`

| 字段 | 前端类型 | 说明 |
| --- | --- | --- |
| `pageNum` | `number` | 页码 |
| `pageSize` | `number` | 每页条数 |
| `searchKey` | `string` | 匹配应用编码/名称 |
| `appCode` | `string` | 编码 |
| `appName` | `string` | 名称 |
| `status` | `number` | 状态 |
| `userType` | `number` | 用户类型 |

### 7.2 应用分页列表

- 请求：`POST /apis/v1/auth/s/apps/page`
- 鉴权：是
- 权限：`app:list`

### 7.3 应用详情

- 请求：`GET /apis/v1/auth/s/apps/{id}`
- 鉴权：是
- 权限：`app:view`

### 7.4 新增应用

- 请求：`POST /apis/v1/auth/s/apps`
- 鉴权：是
- 权限：`app:add`

注意事项：

- `status` 默认 `1`
- `userType` 默认 `1`

### 7.5 编辑应用

- 请求：`PUT /apis/v1/auth/s/apps/{id}`
- 鉴权：是
- 权限：`app:edit`

### 7.6 删除应用

- 请求：`DELETE /apis/v1/auth/s/apps/{id}`
- 鉴权：是
- 权限：`app:remove`

删除限制：

- 仍有用户应用授权时不能删除
- 仍有角色归属该 `appCode` 时不能删除

### 7.7 批量删除应用

- 请求：`DELETE /apis/v1/auth/s/apps`
- 鉴权：是
- 权限：`app:remove`
- Body：`string[]`

### 7.8 启用 / 禁用应用

- 请求：`PATCH /apis/v1/auth/s/apps/{id}/status`
- 鉴权：是
- 权限：`app:status`

## 8. 系统参数配置管理

### 8.1 参数对象说明

#### 8.1.1 `ConfigDTO`

| 字段 | 前端类型 | 新增/编辑 | 返回 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | `string` | 否 | 是 | 参数 ID |
| `configName` | `string` | 是 | 是 | 参数名称 |
| `configKey` | `string` | 是 | 是 | 参数键，唯一 |
| `configValue` | `string` | 是 | 是 | 参数值 |
| `configType` | `number` | 否 | 是 | `1系统内置 / 0普通` |
| `remark` | `string` | 否 | 是 | 备注 |

#### 8.1.2 `ConfigQueryDTO`

| 字段 | 前端类型 | 说明 |
| --- | --- | --- |
| `pageNum` | `number` | 页码 |
| `pageSize` | `number` | 每页条数 |
| `searchKey` | `string` | 匹配参数名称/参数键 |
| `configName` | `string` | 参数名称 |
| `configKey` | `string` | 参数键 |
| `configType` | `number` | 是否内置 |

### 8.2 参数分页列表

- 请求：`POST /apis/v1/auth/s/configs/page`
- 鉴权：是
- 权限：`config:list`

### 8.3 参数详情

- 请求：`GET /apis/v1/auth/s/configs/{id}`
- 鉴权：是
- 权限：`config:view`

### 8.4 按参数键查询

- 请求：`GET /apis/v1/auth/s/configs/by-key/{configKey}`
- 鉴权：是
- 权限：`config:view`

前端适用场景：

- 某些页面初始化要按固定 `configKey` 取值
- 例如取默认密码、开关型配置等

### 8.5 新增参数

- 请求：`POST /apis/v1/auth/s/configs`
- 鉴权：是
- 权限：`config:add`

注意事项：

- `configType` 不传时默认 `0`
- `configKey` 唯一

### 8.6 编辑参数

- 请求：`PUT /apis/v1/auth/s/configs/{id}`
- 鉴权：是
- 权限：`config:edit`

注意事项：

- 如果当前记录是系统内置参数（`configType=1`）：
  - 不允许改 `configKey`
  - 不允许把 `configType` 改成 `0`

### 8.7 删除参数

- 请求：`DELETE /apis/v1/auth/s/configs/{id}`
- 鉴权：是
- 权限：`config:remove`

注意事项：

- 系统内置参数不允许删除

### 8.8 前端对接建议

- 参数表格建议列：`configName`、`configKey`、`configValue`、`configType`、`remark`、`updateTime`
- `configType=1` 适合显示成“系统内置”标签，并在编辑/删除按钮上禁用
- 当前代码里没有“刷新缓存 / 立即生效”接口，前端不要假设保存后会有额外刷新动作

## 9. 字典管理

### 9.1 字典类型对象

#### 9.1.1 `DictTypeDTO`

| 字段 | 前端类型 | 新增/编辑 | 返回 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | `string` | 否 | 是 | 字典类型 ID |
| `dictName` | `string` | 是 | 是 | 字典名称 |
| `dictType` | `string` | 是 | 是 | 字典类型编码，唯一 |
| `status` | `number` | 否 | 是 | `1启用 / 0停用` |
| `remark` | `string` | 否 | 是 | 备注 |

#### 9.1.2 `DictDataDTO`

| 字段 | 前端类型 | 新增/编辑 | 返回 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | `string` | 否 | 是 | 字典数据 ID |
| `dictSort` | `number` | 否 | 是 | 排序号，默认 `0` |
| `dictLabel` | `string` | 是 | 是 | 标签 |
| `dictValue` | `string` | 是 | 是 | 值 |
| `dictType` | `string` | 是 | 是 | 所属字典类型编码 |
| `status` | `number` | 否 | 是 | `1启用 / 0停用` |
| `remark` | `string` | 否 | 是 | 备注 |

前端常见映射：

| 场景 | 字段 |
| --- | --- |
| 下拉框 label | `dictLabel` |
| 下拉框 value | `dictValue` |
| 排序 | `dictSort` |

### 9.2 字典类型分页列表

- 请求：`POST /apis/v1/auth/s/dicts/types/page`
- 鉴权：是
- 权限：`dict:type:list`

### 9.3 字典类型详情

- 请求：`GET /apis/v1/auth/s/dicts/types/{id}`
- 鉴权：是
- 权限：`dict:type:view`

### 9.4 新增字典类型

- 请求：`POST /apis/v1/auth/s/dicts/types`
- 鉴权：是
- 权限：`dict:type:add`

### 9.5 编辑字典类型

- 请求：`PUT /apis/v1/auth/s/dicts/types/{id}`
- 鉴权：是
- 权限：`dict:type:edit`

注意事项：

- 若修改了 `dictType` 编码，后端会把该类型下所有字典数据的 `dictType` 一并改掉

### 9.6 删除字典类型

- 请求：`DELETE /apis/v1/auth/s/dicts/types/{id}`
- 鉴权：是
- 权限：`dict:type:remove`

删除限制：

- 该字典类型下还有字典数据时不能删

### 9.7 启用 / 禁用字典类型

- 请求：`PATCH /apis/v1/auth/s/dicts/types/{id}/status`
- 鉴权：是
- 权限：`dict:type:status`

注意事项：

- 修改字典类型状态时，后端会把该类型下所有字典数据状态一并同步

### 9.8 字典数据分页列表

- 请求：`POST /apis/v1/auth/s/dicts/data/page`
- 鉴权：是
- 权限：`dict:data:list`

筛选字段：

- `dictType`
- `dictLabel`
- `dictValue`
- `status`
- `searchKey`

### 9.9 字典数据详情

- 请求：`GET /apis/v1/auth/s/dicts/data/{id}`
- 鉴权：是
- 权限：`dict:data:view`

### 9.10 新增字典数据

- 请求：`POST /apis/v1/auth/s/dicts/data`
- 鉴权：是
- 权限：`dict:data:add`

注意事项：

- 所属字典类型必须存在且启用
- 同一 `dictType` 下：
  - `dictLabel` 唯一
  - `dictValue` 唯一

### 9.11 编辑字典数据

- 请求：`PUT /apis/v1/auth/s/dicts/data/{id}`
- 鉴权：是
- 权限：`dict:data:edit`

### 9.12 删除字典数据

- 请求：`DELETE /apis/v1/auth/s/dicts/data/{id}`
- 鉴权：是
- 权限：`dict:data:remove`

### 9.13 启用 / 禁用字典数据

- 请求：`PATCH /apis/v1/auth/s/dicts/data/{id}/status`
- 鉴权：是
- 权限：`dict:data:status`

### 9.14 按字典类型查询字典数据

- 请求：`GET /apis/v1/auth/s/dicts/data/by-type/{dictType}?status=1`
- 鉴权：是
- 权限：`dict:data:list`

适用场景：

- 表单下拉初始化
- 页面状态字典加载

请求示例：

```http
GET /apis/v1/auth/s/dicts/data/by-type/sys_common_status?status=1
```

前端对接说明：

- 若只要启用项，建议固定传 `status=1`
- 返回已按 `dictSort` 升序

## 10. B 端认证与账户相关接口

### 10.1 登录请求 / 返回对象

#### 10.1.1 `UserLoginDTO`

| 字段 | 前端类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `username` | `string` | 是 | 登录账号 |
| `password` | `string` | 是 | 密码 |
| `device` | `string` | 否 | 设备标识，例如 `PC`、`APP`、`H5` |

#### 10.1.2 `LoginVO`

| 字段 | 前端类型 | 说明 |
| --- | --- | --- |
| `id` | `string` | 用户 ID |
| `username` | `string` | 账号 |
| `nickname` | `string` | 昵称 |
| `avatar` | `string` | 头像 |
| `deptId` | `string` | 部门 ID |
| `tokenName` | `string` | 固定为 `token` |
| `tokenValue` | `string` | token 值 |

#### 10.1.3 `UserPasswordDTO`

| 字段 | 前端类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `oldPassword` | `string` | 是 | 原密码 |
| `newPassword` | `string` | 是 | 新密码 |

#### 10.1.4 `ForgotPasswordDTO`

| 字段 | 前端类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `username` | `string` | 是 | 账号 |
| `newPassword` | `string` | 是 | 新密码 |
| `verificationCode` | `string` | 是 | 验证码 |

### 10.2 后台登录

- 请求：`POST /apis/v1/auth/s/login`
- 鉴权：否
- Content-Type：`application/json`

请求示例：

```json
{
  "username": "zhanglx",
  "password": "123456",
  "device": "PC"
}
```

返回示例：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": "1300000000000000001",
    "username": "zhanglx",
    "nickname": "超级管理员",
    "avatar": null,
    "deptId": "1100000000000000001",
    "tokenName": "token",
    "tokenValue": "f3e5a0f9-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
  }
}
```

前端对接说明：

- 登录成功后，把 `tokenValue` 放到 header `token`
- `device` 可作为多端区分标识，建议后台 Web 固定传 `PC`

注意事项：

- 若用户设置了 `allowConcurrentLogin=0`，新登录会顶掉旧会话
- 若账号禁用，会返回 `403`

### 10.3 后台退出登录

- 请求：`POST /apis/v1/auth/s/logout`
- 鉴权：是

### 10.4 当前登录用户修改密码

- 请求：`POST /apis/v1/auth/s/user/update/password`
- 鉴权：是
- Content-Type：`application/json`

请求示例：

```json
{
  "oldPassword": "123456",
  "newPassword": "New123456"
}
```

前端对接说明：

- `userId` 由后端从登录态注入，前端不用传
- 修改成功后，后端会强制当前用户下线
- 前端收到成功后应跳回登录页

### 10.5 忘记密码

- 请求：`POST /apis/v1/auth/s/forgot-password`
- 鉴权：否
- Content-Type：`application/json`

请求示例：

```json
{
  "username": "zhanglx",
  "newPassword": "New123456",
  "verificationCode": "123456"
}
```

重要说明：

- 当前代码里的后台“验证码校验”还是占位实现，只校验非空
- 也就是联调阶段传任意非空字符串都能通过
- 正式环境请以后端补完真实验证码逻辑为准

### 10.6 当前缺失能力

当前后台认证部分没有提供：

- 获取当前登录用户详情接口
- 获取当前登录用户菜单树接口
- 获取当前登录用户按钮权限接口
- refresh token 接口
- 图形验证码接口

前端若有这些页面初始化需求，需要后端补接口，或者临时用其他接口组合实现。

## 11. C 端会员认证与个人中心

> 这部分接口是真实存在的，但它们是“会员自助接口”，不是后台管理型会员管理接口。  
> 如果后台前端需要“会员列表 / 会员详情 / 会员状态切换”，当前代码并未提供。

### 11.1 会员对象说明

#### 11.1.1 `MemberLoginDTO`

| 字段 | 前端类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `phoneNumber` | `string` | 是 | 中国大陆手机号 |
| `password` | `string` | 是 | 密码 |
| `device` | `string` | 否 | 设备标识，例如 `H5` |

#### 11.1.2 `MemberRegisterDTO`

| 字段 | 前端类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `phoneNumber` | `string` | 是 | 中国大陆手机号 |
| `password` | `string` | 是 | 6-32 位，且必须同时包含字母和数字 |
| `code` | `string` | 是 | 短信验证码 |
| `device` | `string` | 否 | 设备标识 |

#### 11.1.3 `MemberVerificationCodeSendDTO`

| 字段 | 前端类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `phoneNumber` | `string` | 是 | 手机号 |
| `scene` | `string` | 是 | `REGISTER` / `FORGOT_PASSWORD` / `BIND_PHONE` |

#### 11.1.4 `MemberForgotPasswordDTO`

| 字段 | 前端类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `phoneNumber` | `string` | 是 | 手机号 |
| `newPassword` | `string` | 是 | 新密码，规则同注册 |
| `verificationCode` | `string` | 是 | 验证码 |

#### 11.1.5 `MemberBindPhoneDTO`

| 字段 | 前端类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `phoneNumber` | `string` | 是 | 新手机号 |
| `verificationCode` | `string` | 是 | 验证码 |

#### 11.1.6 `MemberInfoVO`

| 字段 | 前端类型 | 说明 |
| --- | --- | --- |
| `id` | `string` | 会员 ID |
| `phoneNumber` | `string` | 手机号 |
| `phoneBound` | `boolean` | 是否已绑手机 |
| `status` | `number` | 状态：`1正常 / 0禁用` |
| `registerIp` | `string` | 注册 IP |
| `lastLoginTime` | `string` | 最后登录时间 |
| `createTime` | `string` | 注册时间 |

#### 11.1.7 验证码规则

根据配置：

| 项 | 值 |
| --- | --- |
| 验证码有效期 | `300` 秒 |
| 重发间隔 | `60` 秒 |
| 长度 | `6` 位 |
| 发送模式 | 当前 `mockSendEnabled=true`，即 mock 发送 |

### 11.2 会员登录

- 请求：`POST /apis/v1/auth/m/login`
- 鉴权：否

### 11.3 会员注册

- 请求：`POST /apis/v1/auth/m/register`
- 鉴权：否

### 11.4 发送会员验证码

- 请求：`POST /apis/v1/auth/m/verification-code/send`
- 鉴权：否；若 `scene=BIND_PHONE` 则要求已登录会员

请求示例：

```json
{
  "phoneNumber": "13800138000",
  "scene": "REGISTER"
}
```

前端对接说明：

- `REGISTER`：注册前发送
- `FORGOT_PASSWORD`：忘记密码前发送
- `BIND_PHONE`：已登录会员绑定新手机号前发送

### 11.5 微信登录

- 请求：`POST /apis/v1/auth/m/wechat/login?code=xxx`
- 鉴权：否
- 参数位置：Query，不是 Body

### 11.6 会员退出登录

- 请求：`POST /apis/v1/auth/m/logout`
- 鉴权：是（会员 token）

### 11.7 会员修改密码

- 请求：`POST /apis/v1/auth/m/user/update/password`
- 鉴权：是（会员 token）

### 11.8 会员忘记密码

- 请求：`POST /apis/v1/auth/m/forgot-password`
- 鉴权：否

### 11.9 当前会员信息

- 请求：`GET /apis/v1/auth/m/users/current`
- 鉴权：是（会员 token）

### 11.10 更新当前会员资料

- 请求：`PUT /apis/v1/auth/m/users/current`
- 鉴权：是（会员 token）

Body：

```json
{
  "phoneNumber": "13800138000"
}
```

重要说明：

- 这个接口当前仅保留兼容字段 `phoneNumber`
- 实际逻辑中，若想改手机号，必须调用“绑定手机”接口
- 换句话说，这个更新接口目前几乎是只校验不真正更新

### 11.11 绑定当前会员手机号

- 请求：`POST /apis/v1/auth/m/users/current/bind-phone`
- 鉴权：是（会员 token）

### 11.12 注销当前会员

- 请求：`DELETE /apis/v1/auth/m/users/current`
- 鉴权：是（会员 token）

行为说明：

- 会删除会员第三方账号绑定
- 会逻辑删除会员账号
- 会强制退出登录

## 12. 页面与联调建议

### 12.1 路由与页面建议

| 页面 | 推荐接口调用顺序 |
| --- | --- |
| 后台登录页 | `POST /apis/v1/auth/s/login` |
| 用户列表页 | 先调 `POST /users/page`；若有部门筛选，再调 `GET /depts/tree` |
| 新增/编辑用户 | 先调 `GET /depts/tree`；编辑时再调 `GET /users/{id}` |
| 用户分配应用 | 先调 `GET /bindings/users/{userId}/apps`，再调 `POST /apps/page` |
| 用户分配岗位 | 先调 `GET /bindings/users/{userId}/posts`，再调 `POST /posts/page` |
| 角色列表页 | `POST /roles/page` |
| 角色编辑页 | `GET /roles/{id}` |
| 角色分配用户 | `GET /roles/{roleId}/users` + `POST /users/page` |
| 角色分配菜单/权限 | `GET /roles/{id}` + `GET /permissions/tree` |
| 角色数据范围部门 | `GET /roles/{id}` 判断 `dataScope`，若为 `5` 再调 `GET/PUT /bindings/roles/{roleId}/depts` + `GET /depts/tree` |
| 权限管理页 | `GET /permissions/tree` |
| 部门树页 | `GET /depts/tree` |
| 岗位页 | `POST /posts/page` |
| 应用页 | `POST /apps/page` |
| 参数页 | `POST /configs/page` |
| 字典类型页 | `POST /dicts/types/page` |
| 字典数据页 | `POST /dicts/data/page` 或 `GET /dicts/data/by-type/{dictType}` |

### 12.2 前端实现时最容易踩坑的点

1. 所有 ID 必须按 `string` 处理。
2. 批量删除接口 body 是 `string[]`，不是 `{ ids: [] }`。
3. 用户应用绑定传的是 `appCode[]`，不是 `appId[]`。
4. 角色绑定菜单不是单独菜单接口，而是绑定权限树。
5. 很多 `PUT` 接口更像“整对象覆盖更新”，不要只传部分字段。
6. 用户状态、角色状态、部门状态、岗位状态、权限状态都走独立 `PATCH /status`。
7. `GET /permissions/tree` 的 `searchKey` 不是模糊搜索，是精确匹配。
8. 角色绑定用户 / 权限后，建议重新调 GET 接口回显，不要只信 PUT 返回。
9. 部门禁用会级联禁用子部门。
10. 角色 `dataScope != 5` 时，不要展示“自定义部门授权”弹窗。

### 12.3 组件映射建议

| 场景 | label | value / key | children |
| --- | --- | --- | --- |
| 部门树 | `deptName` | `id` | `children` |
| 权限树 | `name` | `id` | `children` |
| 应用下拉 | `appName` | `appCode` | - |
| 岗位多选 | `postName` | `id` | - |
| 字典下拉 | `dictLabel` | `dictValue` | - |

### 12.4 状态开关建议

适合做 switch 的字段：

- 用户 `status`
- 角色 `status`
- 权限 `status`
- 部门 `status`
- 岗位 `status`
- 应用 `status`
- 字典类型 `status`
- 字典数据 `status`

不建议直接用 switch 的字段：

- `configType`，它是“是否内置”，不是启停状态
- `allowConcurrentLogin`，它更像登录策略设置

## 附录：建议后端补充或优化的点

以下内容不影响当前主文档使用，但从前端联调角度看，建议后端后续补齐或优化：

1. 缺少后台管理型 C 端会员管理接口。  
   当前只有会员自助接口，没有会员列表、会员详情、会员禁用、会员重置密码等后台能力。

2. 缺少“用户直接绑定角色”的接口。  
   当前只有“角色 -> 用户”的绑定，没有“用户 -> 角色”的查询/保存接口。做用户编辑页时不够顺手。

3. 缺少后台当前登录人信息 / 菜单 / 权限初始化接口。  
   常见后台前端通常需要：
   - 当前登录人资料
   - 当前登录人菜单树
   - 当前登录人按钮权限  
   当前代码没有直接 HTTP 接口。

4. 没有独立菜单管理接口，菜单与权限完全混用。  
   现在虽然能用，但前后端语义上容易混淆。最好补一层菜单视角的接口/VO。

5. `UserDTO.birthday` 当前不落库，编辑接口也未更新该字段。  
   DTO 暴露了字段，但 Service/PO 实现不完整，前端容易误以为可用。

6. 会员“更新当前资料”接口几乎是 no-op。  
   `PUT /apis/v1/auth/m/users/current` 目前只做手机号兼容校验，不真正修改其它资料。

7. 角色绑定用户接口返回值不完整。  
   `PUT /roles/{roleId}/users` 当前返回的 `RoleInfoVO` 不可靠地带回 `userIds`，前端需要额外重新查询。

8. 角色绑定权限接口返回值不完整。  
   `PUT /roles/{roleId}/permissions` 只返回角色基础信息，不返回最新权限列表，前端仍需再次调详情。

9. 权限编辑接口对“整对象提交”的依赖较强。  
   目前实现更像全量覆盖更新，不适合前端做局部 PATCH；同时 `parentId / identityLineage` 的处理仍偏脆弱。

10. 后台忘记密码验证码还是占位实现。  
   当前只校验“非空”，联调可用，但正式环境安全性不足。

11. 分页对象定义了 `sortingFields`，但大多数分页接口没有真正使用。  
   如果前端做表头排序，目前很可能无法生效。

12. 权限导入/导出 Service 已存在，但没有暴露 Controller 接口。  
   如果前端要做导入导出按钮，当前无法直接联调。

13. 权限树接口会结合当前登录人权限做过滤。  
   对管理端的“全量配置页”来说，普通角色联调时可能拿不到完整树，建议单独提供管理视角的全量树接口。

14. 用户应用绑定使用 `appCode`，其它绑定多使用 ID，风格不一致。  
   前端容易混淆，建议统一成一种绑定主键策略。
