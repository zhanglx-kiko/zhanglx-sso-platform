# XSS 全局防护落地说明

## 1. 改造结论

本次已将原先沉淀在 `sso-web` 中的 XSS 能力完整抽取为独立安全 starter：`sso-xss`。
当前业务应用通过显式依赖 `sso-xss` 获得全局 XSS 防护能力，`sso-web` 不再承载 XSS 相关代码，也不再隐式提供 XSS 能力。

当前落地结果如下：

- 全局防护模块：`sso-xss`
- 业务接入模块：当前已完成 `sso-auth`
- 配置前缀：`sso.xss.*`
- MVC 主链路覆盖入口：`Query / Form / JSON Body / PathVariable / 指定请求头 / multipart 文本分片`
- 主要策略：`TEXT / SEARCH / RICH_TEXT / NONE`
- 审计指标：已接入 Micrometer，可通过 Actuator / Prometheus 采集

## 2. 模块边界

### 2.1 新模块

XSS 能力已经迁移到以下包：

- `com.zhanglx.sso.xss.annotation.XssPolicy`
- `com.zhanglx.sso.xss.config.XssProtectionProperties`
- `com.zhanglx.sso.xss.filter.XssProtectionFilter`
- `com.zhanglx.sso.xss.handler.XssRequestBodyAdvice`
- `com.zhanglx.sso.xss.interceptor.XssPathVariableInterceptor`
- `com.zhanglx.sso.xss.resolver.XssMultipartStringPartResolver`
- `com.zhanglx.sso.xss.support.*`
- `com.zhanglx.sso.xss.wrapper.XssHttpServletRequestWrapper`

### 2.2 已完成的模块切换

- `sso-auth` 已显式依赖 `sso-xss`
- `sso-auth` 中原来引用 `com.zhanglx.sso.web.annotation.XssPolicy`、`com.zhanglx.sso.web.support.XssPolicyMode` 的位置已切换到 `com.zhanglx.sso.xss.*`
- `sso-web` 中原有 XSS 代码和对应测试已移除

## 3. 全局处理链路

### 3.1 Query / Form / 请求头

入口组件：`XssProtectionFilter` + `XssHttpServletRequestWrapper`

职责：

- 包装 `HttpServletRequest`
- 统一清洗 Query 参数
- 统一清洗表单参数
- 对配置允许的请求头做文本清洗
- 在请求结束时统一刷 XSS 审计指标

### 3.2 PathVariable

入口组件：`XssPathVariableInterceptor`

职责：

- 在 `HandlerMethod` 进入前读取 `HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE`
- 按字段策略对路径变量进行清洗
- 对不可变 Map 做复制后回写，避免不同 Spring 链路下直接修改失败

### 3.3 JSON Body / multipart JSON 分片

入口组件：`XssRequestBodyAdvice`

职责：

- 仅在请求体反序列化完成后递归处理对象图
- 覆盖普通字符串、嵌套对象、集合、Map、数组
- 支持字段级 `@XssPolicy`
- 自动识别 multipart JSON 文本分片，统计来源为 `multipart_part`

### 3.4 multipart 文本分片

入口组件：`XssMultipartStringPartResolver`

职责：

- 专门覆盖 `@RequestPart String` 这类文本分片
- 优先从 Spring 已解析的 multipart 请求中读取文本值
- 只清洗文本分片，不处理 `MultipartFile` 二进制内容

这是本次新增的补位能力。原因是：`@RequestPart String` 在当前链路下不能稳定依赖统一 `RequestBodyAdvice` 完成清洗，如果不单独补位，会出现 multipart 文本分片漏拦截。

## 4. 策略说明

### 4.1 策略枚举

- `TEXT`：普通文本，默认策略，清理危险标签、事件属性和危险协议
- `SEARCH`：搜索文本，尽量保留原始搜索语义，只清理明显标签型注入
- `RICH_TEXT`：富文本，按白名单保留安全标签与属性
- `NONE`：完全放行，适用于密码、token、secret 等不应被改写的原文

### 4.2 默认字段策略

默认放行字段：

- `password`
- `oldPassword`
- `newPassword`
- `confirmPassword`
- `credential`
- `token`
- `accessToken`
- `refreshToken`
- `secret`
- `clientSecret`
- `authorization`

默认搜索字段：

- `searchKey`
- `keyword`
- `query`
- `q`

默认富文本字段：

- 当前项目默认未全局放开富文本字段
- 需要显式通过配置或字段注解声明

## 5. 配置项

当前仍使用原有前缀，不需要改配置名：

```yaml
sso:
  xss:
    enabled: true
    global-enabled: true
    log-hit: false
    mode: RELAXED
    whitelist-paths: []
    whitelist-fields:
      - password
      - token
    search-fields:
      - searchKey
      - keyword
    rich-text-fields: []
    sanitize-header-names:
      - User-Agent
      - Referer
      - X-Requested-With
    ignored-content-types:
      - application/octet-stream
      - application/pdf
      - image/*
      - audio/*
      - video/*
```

说明：

- `enabled=false`：`sso-xss` 整个模块不装配
- `global-enabled=false`：保留模块和扩展点，但不执行全局自动清洗
- `ignored-content-types`：忽略二进制内容，避免误把文件流当文本处理
- `multipart/form-data` 不会被整体忽略，而是只清洗文本分片

## 6. 审计指标

### 6.1 已接入指标

已接入的 Micrometer 指标：

- `sso.xss.hit.total`
- `sso.xss.hit.whitelist`
- `sso.xss.request.hit`

### 6.2 指标标签

已严格控制标签基数，只允许以下标签：

- `endpoint`：使用 `HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE` 的接口模板
- `source`：`query_or_form`、`json_body`、`path_variable`、`request_header`、`multipart_part`
- `policy`：`text`、`search`、`rich_text`
- `reason`：`whitelist_path`、`whitelist_field`、`annotation_none`、`ignored_content_type`

注意：

- 不使用原始 URI 作为 endpoint 标签值，避免指标爆炸
- 过滤器阶段拿不到接口模板时，先在 request attribute 内聚合，请求结束后统一落指标

### 6.3 当前内存统计

`XssAuditMetrics` 还保留了实例内的统计信息，便于后续扩展：

- 总命中次数
- 白名单命中次数
- 请求级命中次数
- 接口维度命中分布

## 7. 输入清洗与输出转义边界

本次改造仍坚持下面这条边界：

- 后端负责输入清洗、防止明显危险内容进入业务链路
- 前端负责展示安全，不信任任何可执行 HTML，不随意用 `v-html` / `dangerouslySetInnerHTML`

这意味着：

- 普通文本字段不会因为“后端已经清洗过”就允许前端直接按 HTML 渲染
- 富文本必须前后端共同约定字段级协议，不能临时口头约定
- 不允许把“输入清洗”和“展示转义”混成一个责任

## 8. 测试覆盖

当前 `sso-xss` 已完成并通过以下测试：

- 单元测试：`XssSanitizationServiceTest`
- 单元测试：`XssHttpServletRequestWrapperTest`
- 单元测试：`XssRequestBodyAdviceTest`
- 自动装配测试：`XssAutoConfigTest`
- MockMvc 集成测试：`XssMockMvcIntegrationTest`

当前已覆盖场景：

- Query 参数
- Form 表单
- PathVariable
- JSON Body
- 白名单路径
- 审计指标计数
- multipart `@RequestParam` 文本字段
- multipart `@RequestPart String` 文本分片
- multipart `@RequestPart DTO` JSON 分片
- `MultipartFile` 二进制文件分片不误伤

本轮 `sso-xss` 共通过 21 个测试用例，失败数 0。

## 9. 已确认更新的文档

本轮已确认并完成以下文档更新：

- `docs/XSS全局防护落地说明.md`
- `docs/前端XSS协同接入说明.md`
- `docs/XSS富文本字段协议清单.md`

结论：`前端XSS协同接入说明.md` 需要更新，且已经更新完成。

## 10. 新应用接入方式

未来新的 Servlet 业务应用接入时，推荐按以下步骤：

1. 增加 `sso-xss` 依赖
2. 打开 `sso.xss.enabled=true`
3. 保持或补充 `sso.xss.*` 配置
4. 对密码、token、富文本等特殊字段补充 `@XssPolicy`
5. 如有富文本场景，先登记字段级协议清单再联调
6. 通过 MockMvc 或接口联调验证 Query / Form / JSON / multipart 场景

不建议：

- 再依赖 `sso-web` 隐式获得 XSS 能力
- 让业务开发各自手写 XSS 清洗
- 把富文本字段直接按普通文本字段接入
