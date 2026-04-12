import request from '@/utils/request'
import type {
  AuthSessionStatusVO,
  ForgotPasswordDTO,
  ForgotPasswordVerificationCodeSendDTO,
  ForgotPasswordVerificationCodeVerifyDTO,
  LoginDTO,
  LoginVO,
  SmsVerificationCodeSendVO,
  UserPasswordDTO,
} from '@/types/auth'

export const loginApi = (data: LoginDTO) => {
  return request.post<unknown, LoginVO>('/apis/v1/auth/s/login', data)
}

export const getAuthSessionStatusApi = () => {
  return request.get<unknown, AuthSessionStatusVO>('/apis/v1/auth/isLogin')
}

export const logoutApi = () => {
  return request.post<unknown, void>('/apis/v1/auth/s/logout')
}

export const updatePasswordApi = (data: UserPasswordDTO) => {
  return request.post<unknown, void>('/apis/v1/auth/s/user/update/password', data)
}

export const forgotPasswordApi = (data: ForgotPasswordDTO) => {
  return request.post<unknown, void>('/apis/v1/auth/s/forgot-password', data)
}

export const sendForgotPasswordVerificationCodeApi = (data: ForgotPasswordVerificationCodeSendDTO) => {
  return request.post<unknown, SmsVerificationCodeSendVO>(
    '/apis/v1/auth/s/forgot-password/verification-code/send',
    data,
  )
}

export const verifyForgotPasswordVerificationCodeApi = (
  data: ForgotPasswordVerificationCodeVerifyDTO,
) => {
  return request.post<unknown, void>(
    '/apis/v1/auth/s/forgot-password/verification-code/verify',
    data,
  )
}

export const resetPasswordApi = (userId: string) => {
  return request.post<unknown, void>(`/apis/v1/auth/s/user/reset-password/${userId}`)
}
