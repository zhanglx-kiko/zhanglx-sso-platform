import request from '@/utils/request'
import type { PageResult, StatusPayload } from '@/types/common'
import type {
  RoleDTO,
  RoleInfoVO,
  RolePageQueryDTO,
  RolePermissionRelationshipMappingDTO,
} from '@/types/system'

export const getRolePageApi = (data: RolePageQueryDTO & Record<string, unknown>) => {
  return request.post<unknown, PageResult<RoleDTO>>('/apis/v1/auth/s/roles/page', data)
}

export const getRoleDetailApi = (roleId: string) => {
  return request.get<unknown, RoleDTO>(`/apis/v1/auth/s/roles/${roleId}`)
}

export const createRoleApi = (data: RoleDTO) => {
  return request.post<unknown, RoleDTO>('/apis/v1/auth/s/roles', data)
}

export const updateRoleApi = (roleId: string, data: RoleDTO) => {
  return request.put<unknown, void>(`/apis/v1/auth/s/roles/${roleId}`, data)
}

export const deleteRoleApi = (roleId: string) => {
  return request.delete<unknown, void>(`/apis/v1/auth/s/roles/${roleId}`)
}

export const batchDeleteRolesApi = (roleIds: string[]) => {
  return request.delete<unknown, void>('/apis/v1/auth/s/roles', {
    data: roleIds,
  })
}

export const updateRoleStatusApi = (roleId: string, data: StatusPayload) => {
  return request.patch<unknown, void>(`/apis/v1/auth/s/roles/${roleId}/status`, data)
}

export const getRoleUsersApi = (roleId: string) => {
  return request.get<unknown, RoleInfoVO>(`/apis/v1/auth/s/roles/${roleId}/users`)
}

export const updateRoleUsersApi = (roleId: string, userIds: string[]) => {
  return request.put<unknown, RoleInfoVO>(`/apis/v1/auth/s/roles/${roleId}/users`, userIds)
}

export const updateRolePermissionsApi = (
  roleId: string,
  permissions: RolePermissionRelationshipMappingDTO[],
) => {
  return request.put<unknown, RoleDTO>(`/apis/v1/auth/s/roles/${roleId}/permissions`, permissions)
}

export const getMyRolesApi = () => {
  return request.get<unknown, RoleDTO[]>('/apis/v1/auth/s/roles/my')
}

// Compatibility exports for legacy pages kept in the repository.
export const bindUsersToRoleApi = updateRoleUsersApi
export const getRoleWithUsersApi = getRoleUsersApi
export const getRolesByUserIdApi = async (_userId: string) => [] as RoleDTO[]
