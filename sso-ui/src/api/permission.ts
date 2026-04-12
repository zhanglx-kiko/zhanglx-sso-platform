import request from '@/utils/request'
import type { StatusPayload } from '@/types/common'
import type { PermissionDTO, PermissionQueryDTO, PermissionVO } from '@/types/system'

export const getPermissionTreeApi = (searchKey?: string) => {
  return request.get<unknown, PermissionDTO[]>('/apis/v1/auth/s/permissions/tree', {
    params: { searchKey },
  })
}

export const getPermissionDetailApi = (permissionId: string) => {
  return request.get<unknown, PermissionDTO>(`/apis/v1/auth/s/permissions/${permissionId}`)
}

export const createPermissionApi = (data: PermissionDTO) => {
  return request.post<unknown, PermissionDTO>('/apis/v1/auth/s/permissions', data)
}

export const updatePermissionApi = (permissionId: string, data: PermissionDTO) => {
  return request.put<unknown, void>(`/apis/v1/auth/s/permissions/${permissionId}`, data)
}

export const deletePermissionApi = (permissionId: string) => {
  return request.delete<unknown, void>(`/apis/v1/auth/s/permissions/${permissionId}`)
}

export const batchDeletePermissionsApi = (permissionIds: string[]) => {
  return request.delete<unknown, void>('/apis/v1/auth/s/permissions', {
    data: permissionIds,
  })
}

export const updatePermissionStatusApi = (permissionId: string, data: StatusPayload) => {
  return request.patch<unknown, void>(`/apis/v1/auth/s/permissions/${permissionId}/status`, data)
}

export const getCurrentPermissionsApi = () => {
  return request.get<unknown, PermissionVO[]>('/apis/v1/auth/s/permissions/current')
}

export const getPermissionsByIdentificationApi = (data: PermissionQueryDTO) => {
  return request.post<unknown, PermissionVO[]>('/apis/v1/auth/s/permissions/by-identification', data)
}
