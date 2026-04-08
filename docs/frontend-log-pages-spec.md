# 前端实施说明：登录日志 / 操作日志页面

本文档面向前端开发，目标是让前端基于现有后端接口，直接完成“登录日志”和“操作日志”页面开发，并正确处理操作日志的 `search_after` 深分页交互。

## 1. 页面定位与路由建议

### 1.1 推荐实现方式

推荐在前端实现一个统一页面：

- 路由：`/system/auth/log`
- 页面组件路径：`system/auth/log/index`
- 页面形式：顶部 `Tab` 分成“登录日志”“操作日志”

这样可以与后端已新增的菜单权限保持一致：

- 菜单权限：`system:auth:log`
- 登录日志查询权限：`login-log:list`
- 登录日志详情权限：`login-log:view`
- 操作日志查询权限：`operation-log:list`
- 操作日志详情权限：`operation-log:view`

### 1.2 不推荐拆成随机多级路由

后端 SQL 当前只下发了一个菜单节点“日志审计”，所以前端不要先拆成多个一级菜单。推荐保持一个菜单、两个 Tab。

## 2. 接口响应约定

所有接口都会经过统一响应包装，前端按下面结构处理：

```ts
interface ApiResult<T> {
  code: number
  msg: string
  data: T
}
```

成功判断：

- `code === 200` 视为成功
- 其他状态码按错误提示处理

## 3. 登录日志页面

### 3.1 接口

分页查询：

- `POST /apis/v1/auth/s/login-logs/page`

详情：

- `GET /apis/v1/auth/s/login-logs/{id}`

### 3.2 查询表单

建议字段：

- 用户ID `userId`
- 用户名 `username`
- 事件类型 `eventType`
- 登录结果 `loginResult`
- 登录 IP `loginIp`
- 时间范围 `startTime` / `endTime`

枚举建议：

- `eventType`
  - `LOGIN`：登录
  - `LOGOUT`：登出

- `loginResult`
  - `SUCCESS`：成功
  - `FAILURE`：失败

### 3.3 查询请求示例

```json
{
  "pageNum": 1,
  "pageSize": 20,
  "userId": "1300000000000000001",
  "username": "admin",
  "eventType": "LOGIN",
  "loginResult": "SUCCESS",
  "loginIp": "192.168.1.10",
  "startTime": "2026-04-01 00:00:00",
  "endTime": "2026-04-08 23:59:59"
}
```

### 3.4 列表字段

建议表格列：

- 日志ID `id`
- 用户ID `userId`
- 用户名 `username`
- 展示名 `displayName`
- 事件类型 `eventType`
- 结果 `loginResult`
- 登录IP `loginIp`
- 设备类型 `deviceType`
- 客户端类型 `clientType`
- 应用编码 `appCode`
- 登录时间 `loginTime`
- 登出时间 `logoutTime`
- traceId `traceId`
- requestId `requestId`
- 操作列：详情

### 3.5 列表展示建议

- `eventType`、`loginResult` 使用颜色标签
- `traceId`、`requestId` 支持复制
- `failReason` 不建议直接在主表格完整展开，避免撑宽列表

### 3.6 详情抽屉

点击“详情”打开右侧抽屉或对话框，展示：

- 基础信息：ID、用户、事件类型、结果、应用编码
- 网络信息：登录IP、UserAgent、设备类型
- 链路信息：traceId、requestId
- 时间信息：loginTime、logoutTime、createTime
- 扩展信息：`extJson`
- 失败原因：`failReason`

### 3.7 分页方式

登录日志使用普通分页即可，直接驱动分页组件：

- `current` ← `data.current`
- `pageSize` ← `data.size`
- `total` ← `data.total`

不需要 `search_after`。

## 4. 操作日志页面

### 4.1 接口

分页查询：

- `POST /apis/v1/auth/s/operation-logs/page`

详情：

- `GET /apis/v1/auth/s/operation-logs/{logId}`

### 4.2 查询表单

建议字段：

- 应用编码 `appCode`
- 平台编码 `platformCode`
- 模块 `module`
- 功能 `feature`
- 用户ID `userId`
- 用户名 `username`
- 操作类型 `operationType`
- 结果状态 `resultStatus`
- traceId `traceId`
- 关键字 `keyword`
- 时间范围 `startTime` / `endTime`
- 排序方向 `sortOrder`

排序建议：

- 默认 `desc`
- 只开放：
  - `desc`：最新优先
  - `asc`：最早优先

### 4.3 查询请求示例

```json
{
  "pageNum": 1,
  "pageSize": 20,
  "appCode": "sso",
  "platformCode": "auth-admin",
  "module": "用户管理",
  "feature": "用户",
  "userId": "1300000000000000001",
  "username": "admin",
  "operationType": "UPDATE",
  "resultStatus": "SUCCESS",
  "traceId": "2d3b9b8a7f1e4c83a2e3f8c18c62f7b1",
  "keyword": "/apis/v1/auth/s/users",
  "startTime": "2026-04-01 00:00:00",
  "endTime": "2026-04-08 23:59:59",
  "sortOrder": "desc"
}
```

### 4.4 列表字段

建议主表列：

- `operationName`
- `operationType`
- `module`
- `feature`
- `username`
- `displayName`
- `requestMethod`
- `requestPath`
- `resultStatus`
- `durationMs`
- `clientIp`
- `traceId`
- `endTime`
- 操作列：详情

### 4.5 详情抽屉

详情建议分区显示：

基础信息：

- `logId`
- `appCode / appName`
- `platformCode / platformName`
- `module / feature`
- `operationType / operationName / operationDesc`

操作人信息：

- `userId`
- `username`
- `displayName`
- `tenantId`

请求信息：

- `requestMethod`
- `requestPath`
- `requestQuery`
- `requestBodySummary`
- `clientIp`
- `userAgent`

执行结果：

- `resultStatus`
- `durationMs`
- `responseSummary`
- `errorCode`
- `errorMessageSummary`
- `exceptionType`
- `exceptionStackSummary`

链路信息：

- `traceId`
- `requestId`
- `sourceSystem`
- `startTime`
- `endTime`
- `ingestTime`

扩展字段：

- `ext`

### 4.6 展示细节建议

- `resultStatus`
  - `SUCCESS`：绿色标签
  - `FAILURE`：红色标签

- `requestPath`
  - 列表中单行省略
  - 鼠标悬浮显示完整内容

- `requestBodySummary / responseSummary / exceptionStackSummary`
  - 抽屉中使用代码块或只读文本域
  - 提供“复制”按钮
  - 不允许默认整页展开撑高列表

## 5. 操作日志分页策略

操作日志分页必须做“浅分页 + 深分页”双模式。

### 5.1 后端限制

后端对 ES 查询做了窗口保护：

- 浅分页：支持 `from + size`
- 深分页：超过 `searchMaxWindow` 后，必须使用 `searchAfterToken`

当前默认：

- `searchMaxWindow = 10000`

也就是说：

- 如果 `((pageNum - 1) * pageSize + pageSize) <= 10000`，可以直接按普通分页请求
- 如果超过这个范围，前端必须改用 `searchAfterToken`

### 5.2 后端分页返回

操作日志分页返回：

```ts
interface OperationLogPageVO {
  records: OperationLogVO[]
  total: number
  current: number
  size: number
  nextSearchAfterToken?: string
}
```

注意：

- `nextSearchAfterToken` 表示“当前结果的下一页令牌”
- 这个令牌不是当前页令牌，而是下一页要带的参数

## 6. search_after 前端交互设计

### 6.1 推荐交互规则

推荐采用下面策略：

- 1. 默认保持常规分页组件
- 2. 在浅分页范围内，正常支持页码跳转
- 3. 一旦进入深分页，只允许“上一页 / 下一页”连续翻页
- 4. 深分页模式下，禁用页码输入和随机跳页

原因：

- `search_after` 天然不适合随机跳转
- 随机跳页会导致前端无法凭空生成目标页令牌

### 6.2 必须维护的前端状态

```ts
interface OperationLogPageState {
  pageNum: number
  pageSize: number
  total: number
  sortOrder: 'asc' | 'desc'
  deepPagingMode: boolean
  nextTokenByPage: Record<number, string | undefined>
  pageCache: Record<number, OperationLogVO[]>
  queryFingerprint: string
}
```

说明：

- `nextTokenByPage[1]` 存第一页返回的下一页 token
- 请求第 2 页时，使用 `nextTokenByPage[1]`
- 请求第 3 页时，使用 `nextTokenByPage[2]`
- `pageCache` 用于深分页模式下做“上一页”回退
- `queryFingerprint` 用于判断查询条件是否变更

### 6.3 查询条件变更时必须重置

以下任一项发生变化，都必须清空 token 和缓存，并回到第一页：

- 任意筛选条件变化
- 时间范围变化
- 关键字变化
- `sortOrder` 变化
- `pageSize` 变化
- Tab 从操作日志切到登录日志再切回

重置逻辑：

```ts
function resetOperationLogPagingState() {
  state.pageNum = 1
  state.total = 0
  state.deepPagingMode = false
  state.nextTokenByPage = {}
  state.pageCache = {}
}
```

### 6.4 分页请求算法

#### 场景 A：浅分页

请求条件：

- `targetPage * pageSize <= 10000`

请求体：

```json
{
  "pageNum": 3,
  "pageSize": 20,
  "searchAfterToken": null
}
```

处理逻辑：

- 正常请求
- 保存 `data.nextSearchAfterToken` 到 `nextTokenByPage[targetPage]`

#### 场景 B：深分页进入点

当用户从浅分页继续往后翻，下一页开始超过窗口限制时：

- 将 `deepPagingMode = true`
- 下一页请求不再依赖 `pageNum` 的随机跳转能力
- 使用上一页保存的 token 请求

请求体示例：

```json
{
  "pageNum": 501,
  "pageSize": 20,
  "searchAfterToken": "eyJzb3J0IjpbIjIwMjYtMDQtMDggMTQ6MDA6MDAiLCJhYmNkZWYxMjMiXX0"
}
```

说明：

- `pageNum` 仍然可以继续传，用于页面显示
- 真正决定结果游标的是 `searchAfterToken`

### 6.5 “下一页”实现

```ts
async function goNextPage() {
  const targetPage = state.pageNum + 1

  if (!state.deepPagingMode && targetPage * state.pageSize <= 10000) {
    await fetchOperationLogs({ pageNum: targetPage, pageSize: state.pageSize })
    return
  }

  state.deepPagingMode = true

  const token = state.nextTokenByPage[state.pageNum]
  if (!token) {
    window.$message.warning('没有更多数据或缺少下一页游标')
    return
  }

  await fetchOperationLogs({
    pageNum: targetPage,
    pageSize: state.pageSize,
    searchAfterToken: token
  })
}
```

### 6.6 “上一页”实现

`search_after` 不擅长回翻，所以推荐：

- 上一页优先从 `pageCache` 读取
- 如果缓存不存在，则提示“请重新查询”

```ts
function goPrevPage() {
  const targetPage = state.pageNum - 1
  if (targetPage < 1) return

  const cached = state.pageCache[targetPage]
  if (!cached) {
    window.$message.warning('深分页上一页缓存不存在，请重新查询')
    return
  }

  state.pageNum = targetPage
  tableData.value = cached
}
```

### 6.7 随机跳页规则

推荐规则：

- 浅分页：允许跳到任意页
- 深分页：禁用“跳转到第 N 页”

进入深分页后，分页组件建议只保留：

- 上一页
- 下一页
- 当前页码显示
- 总条数显示

并在分页区域显示提示：

`当前已进入深分页模式，仅支持连续上一页/下一页翻页`

## 7. 前端类型定义建议

### 7.1 登录日志

```ts
interface LoginLogRecord {
  id: string
  userId?: string
  username?: string
  displayName?: string
  eventType: 'LOGIN' | 'LOGOUT'
  loginResult: 'SUCCESS' | 'FAILURE'
  failReason?: string
  loginIp?: string
  userAgent?: string
  deviceType?: string
  traceId?: string
  requestId?: string
  clientType?: string
  appCode?: string
  loginTime?: string
  logoutTime?: string
  createTime?: string
  extJson?: string
}
```

### 7.2 操作日志

```ts
interface OperationLogRecord {
  logId: string
  appCode?: string
  appName?: string
  platformCode?: string
  platformName?: string
  module?: string
  feature?: string
  operationType?: string
  operationName?: string
  operationDesc?: string
  userId?: string
  username?: string
  displayName?: string
  tenantId?: string
  requestMethod?: string
  requestPath?: string
  requestQuery?: string
  requestBodySummary?: string
  responseSummary?: string
  resultStatus?: 'SUCCESS' | 'FAILURE'
  errorCode?: string
  errorMessageSummary?: string
  exceptionType?: string
  exceptionStackSummary?: string
  clientIp?: string
  userAgent?: string
  traceId?: string
  requestId?: string
  startTime?: string
  endTime?: string
  durationMs?: number
  sourceSystem?: string
  ext?: Record<string, string>
  ingestTime?: string
}
```

## 8. 页面交互建议

### 8.1 默认值

登录日志：

- 默认时间范围：最近 7 天
- 默认分页：`pageNum=1`，`pageSize=20`

操作日志：

- 默认时间范围：最近 7 天
- 默认排序：`desc`
- 默认分页：`pageNum=1`，`pageSize=20`

### 8.2 工具栏

建议提供：

- 查询
- 重置
- 展开/收起筛选项
- 复制 traceId
- 导出按钮先不要做，后端目前未提供导出接口

### 8.3 空状态

空状态文案建议：

- 登录日志：`暂无符合条件的登录日志`
- 操作日志：`暂无符合条件的操作日志`

### 8.4 错误提示

后端如果因为深分页限制返回 400，前端统一提示：

`当前查询已超过普通分页窗口，请使用连续翻页方式浏览`

## 9. 详情接口调用建议

### 9.1 登录日志详情

点击行“详情”时：

- 直接根据 `id` 调用详情接口
- 不需要带查询条件

### 9.2 操作日志详情

点击行“详情”时：

- 根据 `logId` 调用详情接口
- 推荐把当前 `startTime/endTime` 一起作为 query 参数带上
- 如果前端未带时间范围，后端也能查，但会扫描更宽范围

示例：

`GET /apis/v1/auth/s/operation-logs/{logId}?startTime=2026-04-01%2000:00:00&endTime=2026-04-08%2023:59:59`

## 10. 推荐的前端开发顺序

1. 先完成 `/system/auth/log` 页面的两组 Tab 骨架
2. 实现登录日志查询、列表、详情
3. 实现操作日志浅分页查询、列表、详情
4. 再补 `search_after` 深分页状态管理
5. 最后补复制、格式化 JSON、异常栈折叠等体验细节

## 11. 前端验收清单

- 登录日志 Tab 能分页查询并查看详情
- 操作日志 Tab 能按条件检索并查看详情
- 操作日志浅分页支持随机跳页
- 操作日志深分页支持连续下一页
- 查询条件变更会重置 token 和缓存
- 深分页模式下禁用随机跳页
- 详情页中大字段不会把页面撑坏
- traceId / requestId 支持复制
- 错误码和失败消息有明确提示

## 12. 一句话结论

前端实现时可以把“登录日志”当普通中后台分页列表，把“操作日志”当“带深分页状态机的日志检索页”来做；两者共用一套 Tab 页面即可，但操作日志必须额外维护 `searchAfterToken` 与页面缓存，不能只用普通分页组件硬跳页。
