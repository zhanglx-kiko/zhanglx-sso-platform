import request from '@/utils/request'
import type { PageQuery, PageResult } from '@/types/common'
import type {
  AdminMemberDetailVO,
  AdminMemberForceLogoutDTO,
  AdminMemberListVO,
  AdminMemberQueryDTO,
  AdminMemberStatusUpdateDTO,
  MemberLoginAuditVO,
  MemberManageRecordVO,
  MemberSocialBindingVO,
} from '@/types/system'

export const getMemberPageApi = (data: AdminMemberQueryDTO) => {
  return request.post<unknown, PageResult<AdminMemberListVO>>('/apis/v1/auth/s/members/page', data)
}

export const getMemberDetailApi = (memberId: string) => {
  return request.get<unknown, AdminMemberDetailVO>(`/apis/v1/auth/s/members/${memberId}`)
}

export const getMemberSocialBindingsApi = (memberId: string) => {
  return request.get<unknown, MemberSocialBindingVO[]>(`/apis/v1/auth/s/members/${memberId}/social-bindings`)
}

export const getMemberLoginAuditsApi = (memberId: string, data: PageQuery & Record<string, unknown>) => {
  return request.post<unknown, PageResult<MemberLoginAuditVO>>(
    `/apis/v1/auth/s/members/${memberId}/login-audits/page`,
    data,
  )
}

export const getMemberManageRecordsApi = (memberId: string, data: PageQuery) => {
  return request.post<unknown, PageResult<MemberManageRecordVO>>(
    `/apis/v1/auth/s/members/${memberId}/manage-records/page`,
    data,
  )
}

export const disableMemberApi = (memberId: string, data: AdminMemberStatusUpdateDTO) => {
  return request.patch<unknown, void>(`/apis/v1/auth/s/members/${memberId}/disable`, data)
}

export const enableMemberApi = (memberId: string, data: AdminMemberStatusUpdateDTO) => {
  return request.patch<unknown, void>(`/apis/v1/auth/s/members/${memberId}/enable`, data)
}

export const freezeMemberApi = (memberId: string, data: AdminMemberStatusUpdateDTO) => {
  return request.patch<unknown, void>(`/apis/v1/auth/s/members/${memberId}/freeze`, data)
}

export const unfreezeMemberApi = (memberId: string, data: AdminMemberStatusUpdateDTO) => {
  return request.patch<unknown, void>(`/apis/v1/auth/s/members/${memberId}/unfreeze`, data)
}

export const forceLogoutMemberApi = (memberId: string, data: AdminMemberForceLogoutDTO) => {
  return request.post<unknown, void>(`/apis/v1/auth/s/members/${memberId}/force-logout`, data)
}
