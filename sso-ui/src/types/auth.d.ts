export interface LoginDTO {
  username: string
  password: string
  device?: string
}

export interface LoginVO {
  id: string
  username: string
  nickname: string
  avatar: string | null
  deptId: string | null
  tokenName: string
  tokenValue: string
}

export interface UserPasswordDTO {
  oldPassword: string
  newPassword: string
}

export interface ForgotPasswordDTO {
  username: string
  newPassword: string
  verificationCode: string
}

export interface MemberLoginDTO {
  phoneNumber: string
  password: string
  device?: string
}

export interface MemberRegisterDTO {
  phoneNumber: string
  password: string
  code: string
  device?: string
}

export interface MemberVerificationCodeSendDTO {
  phoneNumber: string
  scene: 'REGISTER' | 'FORGOT_PASSWORD' | 'BIND_PHONE'
}

export interface MemberForgotPasswordDTO {
  phoneNumber: string
  newPassword: string
  verificationCode: string
}

export interface MemberBindPhoneDTO {
  phoneNumber: string
  verificationCode: string
}

export interface MemberInfoVO {
  id: string
  phoneNumber: string
  phoneBound: boolean
  status: number
  registerIp: string | null
  lastLoginTime: string | null
  createTime: string
}

export interface UserDTO {
  id?: string
  username: string
  nickname?: string
  avatar?: string | null
  phoneNumber?: string
  email?: string
  allowConcurrentLogin?: number
  deptId?: string | null
  status?: number
  password?: string
}

export interface UserQueryDTO {
  pageNum: number
  pageSize: number
  searchKey?: string
  username?: string
  deptId?: string
}
