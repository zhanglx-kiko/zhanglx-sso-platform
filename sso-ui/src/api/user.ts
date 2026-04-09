import request from '@/utils/request'
import type { PageResult, StatusPayload } from '@/types/common'
import type { UserCreateDTO, UserDTO, UserPageQueryDTO, UserUpdateDTO } from '@/types/system'

export const getUserPageApi = (data: UserPageQueryDTO) => {
  return request.post<unknown, PageResult<UserDTO>>('/apis/v1/auth/s/users/page', data)
}

export const getUserDetailApi = (userId: string) => {
  return request.get<unknown, UserDTO>(`/apis/v1/auth/s/users/${userId}`)
}

export const createUserApi = (data: UserCreateDTO) => {
  return request.post<unknown, void>('/apis/v1/auth/s/users', data)
}

export const updateUserApi = (userId: string, data: UserUpdateDTO) => {
  return request.put<unknown, void>(`/apis/v1/auth/s/users/${userId}`, data)
}

export const deleteUserApi = (userId: string) => {
  return request.delete<unknown, void>(`/apis/v1/auth/s/users/${userId}`)
}

export const batchDeleteUsersApi = (userIds: string[]) => {
  return request.delete<unknown, void>('/apis/v1/auth/s/users', {
    data: userIds,
  })
}

export const updateUserStatusApi = (userId: string, data: StatusPayload) => {
  return request.patch<unknown, void>(`/apis/v1/auth/s/users/${userId}/status`, data)
}

// Compatibility exports for legacy pages kept in the repository.
export const getUserListApi = getUserPageApi
export const addUserApi = createUserApi
export const updateUserInfoApi = (data: Partial<UserDTO> & { id?: string }) => {
  if (!data.id) {
    return createUserApi(data as UserCreateDTO)
  }

  return updateUserApi(data.id, {
    nickname: data.nickname,
    avatar: data.avatar,
    phoneNumber: data.phoneNumber,
    email: data.email,
    allowConcurrentLogin: data.allowConcurrentLogin,
    deptId: data.deptId,
  })
}
export const removeUserApi = deleteUserApi
