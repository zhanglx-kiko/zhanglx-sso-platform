import type { SelectOption } from '@/types/common'

export const DEFAULT_PAGE_SIZE = 10
export const DEFAULT_BATCH_PAGE_SIZE = 200

export const STATUS_OPTIONS: SelectOption<number>[] = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 },
]

export const GENDER_OPTIONS: SelectOption<number>[] = [
  { label: '未知', value: 0 },
  { label: '男', value: 1 },
  { label: '女', value: 2 },
]

export const ALLOW_CONCURRENT_OPTIONS: SelectOption<number>[] = [
  { label: '允许并发登录', value: 1 },
  { label: '禁止并发登录', value: 0 },
]

export const ROLE_DATA_SCOPE_OPTIONS: SelectOption<number>[] = [
  { label: '全部数据', value: 1 },
  { label: '本部门及以下', value: 2 },
  { label: '本部门', value: 3 },
  { label: '本人', value: 4 },
  { label: '自定义数据范围', value: 5 },
]

export const PERMISSION_TYPE_OPTIONS: SelectOption<number>[] = [
  { label: '平台', value: -1 },
  { label: '模块', value: 0 },
  { label: '菜单', value: 1 },
  { label: '按钮', value: 2 },
  { label: '接口', value: 3 },
]

export const APP_USER_TYPE_OPTIONS: SelectOption<number>[] = [
  { label: '系统用户应用', value: 1 },
  { label: '会员用户应用', value: 2 },
]

export const CONFIG_TYPE_OPTIONS: SelectOption<number>[] = [
  { label: '普通配置', value: 0 },
  { label: '系统内置', value: 1 },
]

export const YES_NO_OPTIONS: SelectOption<number>[] = [
  { label: '否', value: 0 },
  { label: '是', value: 1 },
]

export const MEMBER_STATUS_OPTIONS: SelectOption<number>[] = [
  { label: '禁用', value: 0 },
  { label: '正常', value: 1 },
  { label: '冻结', value: 2 },
  { label: '注销中', value: 3 },
  { label: '已注销', value: 4 },
]

export const MEMBER_REAL_NAME_STATUS_OPTIONS: SelectOption<number>[] = [
  { label: '未认证', value: 0 },
  { label: '认证中', value: 1 },
  { label: '已认证', value: 2 },
  { label: '认证失败', value: 3 },
]

export const MEMBER_TYPE_OPTIONS: SelectOption<number>[] = [
  { label: '普通会员', value: 0 },
  { label: 'VIP会员', value: 1 },
]

export const MEMBER_MANAGE_ACTION_OPTIONS: SelectOption<number>[] = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 2 },
  { label: '冻结', value: 3 },
  { label: '解冻', value: 4 },
  { label: '强制下线', value: 5 },
  { label: '注销', value: 6 },
  { label: '恢复', value: 7 },
  { label: '加入黑名单', value: 8 },
  { label: '移出黑名单', value: 9 },
]

export const MEMBER_PERMISSION_KEYS = {
  LIST: 'member:list',
  VIEW: 'member:view',
  SOCIAL_LIST: 'member:social:list',
  LOGIN_LOG_LIST: 'member:login-log:list',
  MANAGE_RECORD_LIST: 'member:manage-record:list',
  DISABLE: 'member:disable',
  ENABLE: 'member:enable',
  FREEZE: 'member:freeze',
  UNFREEZE: 'member:unfreeze',
  FORCE_LOGOUT: 'member:force-logout',
} as const
