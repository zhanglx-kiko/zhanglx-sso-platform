import request from '@/utils/request'
import type { PageResult, StatusPayload } from '@/types/common'
import type { DeptDTO, DeptQueryDTO } from '@/types/system'

export const getDeptPageApi = (data: DeptQueryDTO) => {
  return request.post<unknown, PageResult<DeptDTO>>('/apis/v1/auth/s/depts/page', data)
}

export const getDeptTreeApi = (params?: Partial<Pick<DeptQueryDTO, 'deptName' | 'status'>>) => {
  return request.get<unknown, DeptDTO[]>('/apis/v1/auth/s/depts/tree', {
    params,
  })
}

export const getDeptDetailApi = (deptId: string) => {
  return request.get<unknown, DeptDTO>(`/apis/v1/auth/s/depts/${deptId}`)
}

export const createDeptApi = (data: DeptDTO) => {
  return request.post<unknown, void>('/apis/v1/auth/s/depts', data)
}

export const updateDeptApi = (deptId: string, data: DeptDTO) => {
  return request.put<unknown, void>(`/apis/v1/auth/s/depts/${deptId}`, data)
}

export const deleteDeptApi = (deptId: string) => {
  return request.delete<unknown, void>(`/apis/v1/auth/s/depts/${deptId}`)
}

export const updateDeptStatusApi = (deptId: string, data: StatusPayload) => {
  return request.patch<unknown, void>(`/apis/v1/auth/s/depts/${deptId}/status`, data)
}
