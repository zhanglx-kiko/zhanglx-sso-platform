import request from '@/utils/request'
import type { PageResult } from '@/types/common'
import type { ConfigDTO, ConfigQueryDTO } from '@/types/system'

export const getConfigPageApi = (data: ConfigQueryDTO) => {
  return request.post<unknown, PageResult<ConfigDTO>>('/apis/v1/auth/s/configs/page', data)
}

export const getConfigDetailApi = (configId: string) => {
  return request.get<unknown, ConfigDTO>(`/apis/v1/auth/s/configs/${configId}`)
}

export const getConfigByKeyApi = (configKey: string) => {
  return request.get<unknown, ConfigDTO>(`/apis/v1/auth/s/configs/by-key/${configKey}`)
}

export const createConfigApi = (data: ConfigDTO) => {
  return request.post<unknown, void>('/apis/v1/auth/s/configs', data)
}

export const updateConfigApi = (configId: string, data: ConfigDTO) => {
  return request.put<unknown, void>(`/apis/v1/auth/s/configs/${configId}`, data)
}

export const deleteConfigApi = (configId: string) => {
  return request.delete<unknown, void>(`/apis/v1/auth/s/configs/${configId}`)
}

export const refreshConfigRuntimeCacheApi = () => {
  return request.post<unknown, void>('/apis/v1/auth/s/configs/runtime/refresh')
}
