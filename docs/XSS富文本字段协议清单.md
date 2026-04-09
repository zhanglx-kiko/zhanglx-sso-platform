# XSS 富文本字段协议清单

## 1. 用途

这份清单是当前项目前后端对齐富文本字段策略的唯一来源。
任何新增富文本字段，都必须先登记到这里，再开始联调和页面开发。

## 2. 当前状态

截至本次改造完成，当前项目默认没有全局放开的富文本字段。
也就是说：

- `sso.xss.rich-text-fields` 当前为空
- `sso-auth` 当前没有新增必须走富文本协议的业务字段
- 未登记字段一律按普通文本处理

## 3. 当前已确认清单

| 应用/模块 | 接口路径 | 请求字段路径 | 是否允许富文本 | 后端策略落点 | 前端组件约束要求 | 允许标签/属性/协议范围 | 联调负责人/备注 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| sso-auth / 全局现状 | 暂无 | 暂无 | 否 | 默认 `TEXT` / `SEARCH` / `NONE` | 普通输入组件，不允许直接渲染 HTML | 不适用 | 当前没有正式富文本字段 |

## 4. 当前项目字段级草案

下面这张表不是“允许富文本”的清单，而是当前项目已经提前确认过的字段边界。
前端开发如果碰到这些字段，不需要二次猜测，直接按这里执行。

| 应用/模块 | 接口路径 | 请求字段路径 | 当前结论 | 后端默认策略 | 前端录入与展示要求 | 升级为富文本的前置条件 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| sso-auth / 应用管理 | `POST /apis/v1/auth/s/apps`、`PUT /apis/v1/auth/s/apps/{id}` | `appName`、`remark` | 禁止富文本 | `TEXT` | 普通输入框；展示层按文本渲染 | 不允许直接升级 | `remark` 仅作为备注，不得渲染 HTML |
| sso-auth / 参数管理 | `POST /apis/v1/auth/s/configs`、`PUT /apis/v1/auth/s/configs/{id}` | `configName`、`configKey`、`remark` | 禁止富文本 | `TEXT` | 普通输入框；展示层按文本渲染 | 不允许直接升级 | 参数名称、参数键、备注都不属于富文本场景 |
| sso-auth / 参数管理 | `POST /apis/v1/auth/s/configs`、`PUT /apis/v1/auth/s/configs/{id}` | `configValue` | 当前禁止富文本 | `TEXT` | 默认按纯文本编辑和展示 | 如未来出现“模板正文 / 公告正文 / 帮助中心正文”这类明确 HTML 需求，必须拆出专用业务字段并先登记本清单 | 当前 `ConfigTypeEnum` 只有普通参数和系统内置参数，不能把 `configValue` 直接当富文本容器使用 |
| sso-auth / 角色管理 | `POST /apis/v1/auth/s/roles`、`PUT /apis/v1/auth/s/roles/{id}` | `roleName`、`roleCode`、`remark` | 禁止富文本 | `TEXT` | 普通输入框；展示层按文本渲染 | 不允许直接升级 | 角色名称、编码、备注都只能是普通文本 |
| sso-auth / 权限管理 | `POST /apis/v1/auth/s/permissions`、`PUT /apis/v1/auth/s/permissions/{id}` | `name`、`identification`、`remark` | 禁止富文本 | `TEXT` | 普通输入框；展示层按文本渲染 | 不允许直接升级 | 权限节点名称和标识不允许携带 HTML |
| sso-auth / 权限管理 | `POST /apis/v1/auth/s/permissions`、`PUT /apis/v1/auth/s/permissions/{id}` | `comPath`、`path` | 禁止富文本 | `TEXT` | 路由/组件路径输入，不允许粘贴 HTML | 不允许直接升级 | 这两个字段属于路径协议字段，只能存路径值 |
| sso-auth / 字典管理 | `POST /apis/v1/auth/s/dicts/types`、`PUT /apis/v1/auth/s/dicts/types/{id}` | `dictName`、`dictType`、`remark` | 禁止富文本 | `TEXT` | 普通输入框；展示层按文本渲染 | 不允许直接升级 | 字典类型字段不允许富文本 |
| sso-auth / 字典管理 | `POST /apis/v1/auth/s/dicts/data`、`PUT /apis/v1/auth/s/dicts/data/{id}` | `dictLabel`、`dictValue`、`remark` | 禁止富文本 | `TEXT` | 普通输入框；展示层按文本渲染 | 不允许直接升级 | 字典标签和值都按纯文本处理 |
| sso-auth / 部门管理 | `POST /apis/v1/auth/s/depts`、`PUT /apis/v1/auth/s/depts/{id}` | `deptName` | 禁止富文本 | `TEXT` | 普通输入框；展示层按文本渲染 | 不允许直接升级 | 部门名称不能出现 HTML 结构 |
| sso-auth / 岗位管理 | `POST /apis/v1/auth/s/posts`、`PUT /apis/v1/auth/s/posts/{id}` | `postCode`、`postName` | 禁止富文本 | `TEXT` | 普通输入框；展示层按文本渲染 | 不允许直接升级 | 岗位编码和岗位名称按纯文本处理 |
| sso-auth / 用户管理 | `POST /apis/v1/auth/s/users` | `username`、`nickname`、`avatar` | 禁止富文本 | `TEXT` | 用户名/昵称按文本渲染，头像只允许 URL 字符串 | 不允许直接升级 | `avatar` 是 URL 字段，不是富文本内容 |
| sso-auth / 用户管理 | `POST /apis/v1/auth/s/users` | `password` | 绝不属于富文本 | `NONE` | 密码框；禁止回显；禁止展示层渲染 | 不允许升级 | 这是敏感字段例外，不参与任何富文本协议 |
| sso-auth / 用户管理 | `PUT /apis/v1/auth/s/users/{userId}` | `nickname`、`avatar`、`deptName` | 禁止富文本 | `TEXT` | 普通输入框或 URL 输入框；展示层按文本渲染 | 不允许直接升级 | `deptName` 是部门名称展示值，不能被当成富文本 |

## 5. 新增字段登记模板

新增富文本字段时，请按下面模板新增一行：

| 应用/模块 | 接口路径 | 请求字段路径 | 是否允许富文本 | 后端策略落点 | 前端组件约束要求 | 允许标签/属性/协议范围 | 联调负责人/备注 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 示例：sso-auth / 公告管理 | `/apis/v1/auth/s/notices` | `content` | 是 | `@XssPolicy(XssPolicyMode.RICH_TEXT)` | 富文本编辑器公共组件，禁止脚本与事件属性 | `p`、`strong`、`em`、`ul`、`ol`、`li`、`a[href=http/https/mailto]`、`img[src=http/https]` | 填写后端/前端负责人 |

## 6. 字段登记规则

### 6.1 必须登记的场景

- 公告正文
- 帮助中心正文
- 模板正文
- 富文本消息正文
- 任何允许录入并展示 HTML 结构的字段

### 6.2 禁止登记成富文本的典型字段

- 标题
- 名称
- 备注
- 描述
- 搜索关键字
- 菜单名称
- 角色名称
- 权限名称
- 标签名称

## 7. 后端落点规则

富文本字段后端只允许两种正式落点：

- 配置方式：加入 `sso.xss.rich-text-fields`
- 字段方式：显式标注 `@XssPolicy(XssPolicyMode.RICH_TEXT)`

密码、token、secret 这类字段不属于富文本，仍应使用默认放行或显式 `NONE`。

## 8. 前端落点规则

- 富文本输入必须使用团队统一封装的编辑器组件
- 富文本展示必须使用统一封装的渲染组件
- 禁止页面自行直接 `v-html` 或拼接 `innerHTML`
- 允许的标签、属性、协议必须与本清单保持一致

## 9. 联调要求

每次新增富文本字段时，至少要完成：

1. 本清单新增登记
2. 后端字段策略落点完成
3. 前端组件约束完成
4. 提供一组允许内容样例
5. 提供一组应被清理的危险内容样例
6. 完成一次接口联调和展示联调

## 10. 变更规则

- 这份清单必须跟代码一起提交
- 没有更新清单的富文本改造，不视为完整交付
- 如果前后端口径不一致，以这份清单为准，先修订清单再继续联调
