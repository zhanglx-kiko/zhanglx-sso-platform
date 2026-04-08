# 限流与真实 IP 方案

## 分层策略

| 类别 | 典型接口 | NGINX | 应用层 | 维度 | 阈值 |
| --- | --- | --- | --- | --- | --- |
| 登录 | `/apis/v1/auth/s/login` | `login_ip_zone` | `@RequestRateLimit` | `IP + URI + 用户名` | NGINX `5/min`，应用层 `5/min` |
| 登出 | `/apis/v1/auth/s/logout` | `session_ip_zone` | `@RequestRateLimit` | `UserId + URI` | NGINX `60/min`，应用层 `20/min` |
| 当前用户读取 | `/apis/v1/auth/current-user` 或等价自助接口 | `session_ip_zone` | 可选 | `UserId + URI` | NGINX `60/min` |
| 后台分页查询 | `/apis/v1/auth/s/users/page` | `admin_read_ip_zone` | `@RequestRateLimit` | `UserId + URI` | NGINX `120/min`，应用层 `60/min` |
| 后台写操作 | 用户/角色/权限/配置新增改删、绑定解绑 | `admin_write_ip_uri_zone` | `@RequestRateLimit + @RepeatSubmit` | `UserId + URI` | NGINX `20/min`，应用层按接口 `5~20/min` |
| 导入导出 | `/**/import`、`/**/export` | `import_export_ip_zone` | 视接口叠加 | `IP + URI` | NGINX `2/min` |

## 真实 IP 规则

1. 边界入口必须由 NGINX 统一透传 `X-Real-IP`、`X-Forwarded-For`、`X-Request-Id`。
2. 后端不直接信任客户端传入头部，只在 `remoteAddr` 落在 `trusted proxies` 网段时解析转发链。
3. 多级代理场景下开启 `real_ip_recursive on`，并在应用层通过 `ClientIpUtils` 从右向左回溯首个非可信代理地址。
4. Docker 本地调试与内网代理统一纳入 `127.0.0.1/32`、`10/8`、`172.16/12`、`192.168/16` 等可信网段。

## 策略边界

- NGINX 负责匿名流量、边界防刷、突刺削峰、连接数限制和粗粒度 IP 治理。
- 应用层负责基于登录态、租户、URI、请求体指纹的精细化分布式限流。
- 重复提交不是 NGINX 职责，统一由公共依赖中的 `@RepeatSubmit` 处理。
