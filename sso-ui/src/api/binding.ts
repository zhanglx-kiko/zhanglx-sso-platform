import request from '@/utils/request'
import type { AppDTO, DeptDTO, PostDTO } from '@/types/system'

export const getUserAppsApi = (userId: string) => {
  return request.get<unknown, AppDTO[]>(`/apis/v1/auth/s/bindings/users/${userId}/apps`)
}

export const updateUserAppsApi = (userId: string, appCodes: string[]) => {
  return request.put<unknown, void>(`/apis/v1/auth/s/bindings/users/${userId}/apps`, appCodes)
}

export const getUserPostsApi = (userId: string) => {
  return request.get<unknown, PostDTO[]>(`/apis/v1/auth/s/bindings/users/${userId}/posts`)
}

export const updateUserPostsApi = (userId: string, postIds: string[]) => {
  return request.put<unknown, void>(`/apis/v1/auth/s/bindings/users/${userId}/posts`, postIds)
}

export const getRoleDeptsApi = (roleId: string) => {
  return request.get<unknown, DeptDTO[]>(`/apis/v1/auth/s/bindings/roles/${roleId}/depts`)
}

export const updateRoleDeptsApi = (roleId: string, deptIds: string[]) => {
  return request.put<unknown, void>(`/apis/v1/auth/s/bindings/roles/${roleId}/depts`, deptIds)
}
