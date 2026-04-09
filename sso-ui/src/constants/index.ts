export const STORAGE_KEYS = {
  TOKEN: 'token',
  USER_INFO: 'userInfo',
} as const

export const TOKEN_HEADER_KEY = 'token'

export const TOKEN_EXPIRED_KEYWORDS = [
  'token 已被冻结',
  'token已被冻结',
  'token已过期',
  'token 失效',
  'token无效',
  'token invalid',
  'token expired',
  '登录已过期',
  '登录状态已失效',
  '认证已失效',
  '已被下线',
  '被踢下线',
  '已下线',
] as const

export const HTTP_STATUS = {
  UNAUTHORIZED: 401,
  SUCCESS: 200,
} as const

export const BIZ_CODE = {
  SUCCESS: 200,
  UNAUTHORIZED: 401,
  TOKEN_EXPIRED: 1001,
} as const

export const ROUTE_WHITE_LIST = ['/login', '/404'] as const

export const AUTH_EVENT_STORAGE_KEY = '__sso_auth_event__'

export const AUTH_EXPIRED_BIZ_CODES = [401, 419, 440, 498, 499, 1001, 1002, 1003, 1004] as const

export const AUTH_EXPIRED_KEYWORDS = [
  'token无效',
  'token 无效',
  'token失效',
  'token 失效',
  'token已过期',
  'token 已过期',
  'token过期',
  'token 过期',
  'token已被冻结',
  'token 已被冻结',
  'token invalid',
  'token expired',
  'access token',
  'session expired',
  'session invalid',
  '未登录',
  '未认证',
  '登录失效',
  '登录状态已失效',
  '登录信息已失效',
  '请重新登录',
  '重新登录',
  '会话失效',
  '会话已失效',
  '认证失败',
  '认证已失效',
  '无访问令牌',
  '权限上下文丢失',
  '身份上下文丢失',
  '被踢下线',
  '踢下线',
  '已下线',
] as const
