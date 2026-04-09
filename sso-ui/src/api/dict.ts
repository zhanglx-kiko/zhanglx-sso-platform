import request from '@/utils/request'
import type { PageResult, StatusPayload } from '@/types/common'
import type { DictDataDTO, DictDataQueryDTO, DictTypeDTO, DictTypeQueryDTO } from '@/types/system'

export const getDictTypePageApi = (data: DictTypeQueryDTO) => {
  return request.post<unknown, PageResult<DictTypeDTO>>('/apis/v1/auth/s/dicts/types/page', data)
}

export const getDictTypeDetailApi = (dictTypeId: string) => {
  return request.get<unknown, DictTypeDTO>(`/apis/v1/auth/s/dicts/types/${dictTypeId}`)
}

export const createDictTypeApi = (data: DictTypeDTO) => {
  return request.post<unknown, void>('/apis/v1/auth/s/dicts/types', data)
}

export const updateDictTypeApi = (dictTypeId: string, data: DictTypeDTO) => {
  return request.put<unknown, void>(`/apis/v1/auth/s/dicts/types/${dictTypeId}`, data)
}

export const deleteDictTypeApi = (dictTypeId: string) => {
  return request.delete<unknown, void>(`/apis/v1/auth/s/dicts/types/${dictTypeId}`)
}

export const updateDictTypeStatusApi = (dictTypeId: string, data: StatusPayload) => {
  return request.patch<unknown, void>(`/apis/v1/auth/s/dicts/types/${dictTypeId}/status`, data)
}

export const getDictDataPageApi = (data: DictDataQueryDTO) => {
  return request.post<unknown, PageResult<DictDataDTO>>('/apis/v1/auth/s/dicts/data/page', data)
}

export const getDictDataDetailApi = (dictDataId: string) => {
  return request.get<unknown, DictDataDTO>(`/apis/v1/auth/s/dicts/data/${dictDataId}`)
}

export const createDictDataApi = (data: DictDataDTO) => {
  return request.post<unknown, void>('/apis/v1/auth/s/dicts/data', data)
}

export const updateDictDataApi = (dictDataId: string, data: DictDataDTO) => {
  return request.put<unknown, void>(`/apis/v1/auth/s/dicts/data/${dictDataId}`, data)
}

export const deleteDictDataApi = (dictDataId: string) => {
  return request.delete<unknown, void>(`/apis/v1/auth/s/dicts/data/${dictDataId}`)
}

export const updateDictDataStatusApi = (dictDataId: string, data: StatusPayload) => {
  return request.patch<unknown, void>(`/apis/v1/auth/s/dicts/data/${dictDataId}/status`, data)
}

export const getDictDataByTypeApi = (dictType: string, status?: number) => {
  return request.get<unknown, DictDataDTO[]>(`/apis/v1/auth/s/dicts/data/by-type/${dictType}`, {
    params: { status },
  })
}
