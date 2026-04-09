import type { PageQuery, PageResult } from '@/types/common'

export type LogResultStatus = 'SUCCESS' | 'FAILURE'
export type LoginEventType = 'LOGIN' | 'LOGOUT'
export type OperationSortOrder = 'asc' | 'desc'

export interface LoginLogQueryDTO extends PageQuery {
  userId?: string
  username?: string
  eventType?: LoginEventType
  loginResult?: LogResultStatus
  loginIp?: string
  startTime?: string
  endTime?: string
}

export interface LoginLogRecord {
  id: string
  userId?: string
  username?: string
  displayName?: string
  eventType: LoginEventType
  loginResult: LogResultStatus
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

export interface OperationLogQueryDTO extends PageQuery {
  appCode?: string
  platformCode?: string
  module?: string
  feature?: string
  userId?: string
  username?: string
  operationType?: string
  resultStatus?: LogResultStatus
  traceId?: string
  keyword?: string
  startTime?: string
  endTime?: string
  sortOrder?: OperationSortOrder
  searchAfterToken?: string
}

export interface OperationLogRecord {
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
  resultStatus?: LogResultStatus
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
  ext?: Record<string, unknown> | string
  ingestTime?: string
}

export interface OperationLogPageVO extends PageResult<OperationLogRecord> {
  nextSearchAfterToken?: string
}

export interface OperationLogPageState {
  pageNum: number
  pageSize: number
  total: number
  sortOrder: OperationSortOrder
  deepPagingMode: boolean
  nextTokenByPage: Record<number, string | undefined>
  pageCache: Record<number, OperationLogRecord[]>
  queryFingerprint: string
}
