import request from '@/utils/request'
import type { PageResult } from '@/types/common'
import type {
  LoginLogQueryDTO,
  LoginLogRecord,
  OperationLogPageVO,
  OperationLogQueryDTO,
  OperationLogRecord,
} from '@/types/log'

export const getLoginLogPageApi = (data: LoginLogQueryDTO) => {
  return request.post<unknown, PageResult<LoginLogRecord>>('/apis/v1/auth/s/login-logs/page', data)
}

export const getLoginLogDetailApi = (id: string) => {
  return request.get<unknown, LoginLogRecord>(`/apis/v1/auth/s/login-logs/${id}`)
}

export const getOperationLogPageApi = (data: OperationLogQueryDTO) => {
  return request.post<unknown, OperationLogPageVO>('/apis/v1/auth/s/operation-logs/page', data)
}

export const getOperationLogDetailApi = (logId: string, params?: { startTime?: string; endTime?: string }) => {
  return request.get<unknown, OperationLogRecord>(`/apis/v1/auth/s/operation-logs/${logId}`, {
    params,
  })
}
