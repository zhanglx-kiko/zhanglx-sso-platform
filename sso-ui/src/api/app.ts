import request from '@/utils/request'
import type { PageResult, StatusPayload } from '@/types/common'
import type { AppDTO, AppQueryDTO } from '@/types/system'

export const getAppPageApi = (data: AppQueryDTO) => {
  return request.post<unknown, PageResult<AppDTO>>('/apis/v1/auth/s/apps/page', data)
}

export const getAppDetailApi = (appId: string) => {
  return request.get<unknown, AppDTO>(`/apis/v1/auth/s/apps/${appId}`)
}

export const createAppApi = (data: AppDTO) => {
  return request.post<unknown, void>('/apis/v1/auth/s/apps', data)
}

export const updateAppApi = (appId: string, data: AppDTO) => {
  return request.put<unknown, void>(`/apis/v1/auth/s/apps/${appId}`, data)
}

export const deleteAppApi = (appId: string) => {
  return request.delete<unknown, void>(`/apis/v1/auth/s/apps/${appId}`)
}

export const batchDeleteAppsApi = (appIds: string[]) => {
  return request.delete<unknown, void>('/apis/v1/auth/s/apps', {
    data: appIds,
  })
}

export const updateAppStatusApi = (appId: string, data: StatusPayload) => {
  return request.patch<unknown, void>(`/apis/v1/auth/s/apps/${appId}/status`, data)
}
