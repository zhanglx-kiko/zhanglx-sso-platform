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

