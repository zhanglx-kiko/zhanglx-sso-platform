import type { SelectOption } from '@/types/common'
import type {
  LoginEventType,
  LogResultStatus,
  LoginLogQueryDTO,
  OperationLogPageState,
  OperationLogQueryDTO,
  OperationSortOrder,
} from '@/types/log'

export const LOG_SEARCH_MAX_WINDOW = 10000
export const LOG_DEFAULT_PAGE_SIZE = 20
export const LOG_DEFAULT_RANGE_DAYS = 7

export const LOGIN_EVENT_OPTIONS: SelectOption<LoginEventType>[] = [
  { label: '登录', value: 'LOGIN' },
  { label: '登出', value: 'LOGOUT' },
]

export const LOG_RESULT_OPTIONS: SelectOption<LogResultStatus>[] = [
  { label: '成功', value: 'SUCCESS' },
  { label: '失败', value: 'FAILURE' },
]

export const LOG_SORT_OPTIONS: SelectOption<OperationSortOrder>[] = [
  { label: '最新优先', value: 'desc' },
  { label: '最早优先', value: 'asc' },
]

const pad = (value: number) => String(value).padStart(2, '0')

export const formatDateTime = (value?: string | null): string => {
  if (!value) return '--'
  return value
}

export const formatDuration = (value?: number | null): string => {
  if (value === undefined || value === null || Number.isNaN(Number(value))) return '--'
  if (value < 1000) return `${value} ms`
  return `${(value / 1000).toFixed(value % 1000 === 0 ? 0 : 2)} s`
}

export const getRelativeDateRange = (days = LOG_DEFAULT_RANGE_DAYS): [string, string] => {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - Math.max(days - 1, 0))
  start.setHours(0, 0, 0, 0)
  end.setHours(23, 59, 59, 0)

  return [toDateTimeString(start), toDateTimeString(end)]
}

export const toDateTimeString = (value: Date): string => {
  return `${value.getFullYear()}-${pad(value.getMonth() + 1)}-${pad(value.getDate())} ${pad(value.getHours())}:${pad(value.getMinutes())}:${pad(value.getSeconds())}`
}

export const stringifyMaybeJson = (value: unknown): string => {
  if (value === undefined || value === null || value === '') return ''

  if (typeof value === 'string') {
    const trimmed = value.trim()
    if (!trimmed) return ''

    try {
      return JSON.stringify(JSON.parse(trimmed), null, 2)
    } catch {
      return trimmed
    }
  }

  try {
    return JSON.stringify(value, null, 2)
  } catch {
    return String(value)
  }
}

export const createOperationLogFingerprint = (query: OperationLogQueryDTO): string => {
  const { searchAfterToken: _searchAfterToken, pageNum: _pageNum, ...rest } = query
  return JSON.stringify(rest)
}

export const resetOperationLogPagingState = (
  state: OperationLogPageState,
  pageSize = LOG_DEFAULT_PAGE_SIZE,
  sortOrder: OperationSortOrder = 'desc',
) => {
  state.pageNum = 1
  state.pageSize = pageSize
  state.total = 0
  state.sortOrder = sortOrder
  state.deepPagingMode = false
  state.nextTokenByPage = {}
  state.pageCache = {}
  state.queryFingerprint = ''
}

export const isDeepPagingRequest = (
  pageNum: number,
  pageSize: number,
  maxWindow = LOG_SEARCH_MAX_WINDOW,
): boolean => {
  return pageNum * pageSize > maxWindow
}

export const buildLoginLogPayload = (
  query: Omit<LoginLogQueryDTO, 'startTime' | 'endTime'>,
  timeRange: string[],
): LoginLogQueryDTO => {
  const [startTime, endTime] = timeRange
  return {
    ...query,
    startTime: startTime || undefined,
    endTime: endTime || undefined,
  }
}

export const buildOperationLogPayload = (
  query: Omit<OperationLogQueryDTO, 'startTime' | 'endTime'>,
  timeRange: string[],
): OperationLogQueryDTO => {
  const [startTime, endTime] = timeRange
  return {
    ...query,
    startTime: startTime || undefined,
    endTime: endTime || undefined,
  }
}

export const getLoginEventMeta = (value?: LoginEventType) => {
  if (value === 'LOGOUT') {
    return { label: '登出', type: 'info' as const }
  }

  return { label: '登录', type: 'primary' as const }
}

export const getLogResultMeta = (value?: LogResultStatus) => {
  if (value === 'FAILURE') {
    return { label: '失败', type: 'danger' as const }
  }

  return { label: '成功', type: 'success' as const }
}

export const getOperationMethodTagType = (value?: string) => {
  const method = String(value || '').toUpperCase()
  if (method === 'GET') return 'info' as const
  if (method === 'POST') return 'success' as const
  if (method === 'PUT' || method === 'PATCH') return 'warning' as const
  if (method === 'DELETE') return 'danger' as const
  return 'info' as const
}
