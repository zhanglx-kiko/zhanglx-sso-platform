import type { BaseEntity, PageQuery } from '@/types/common'

export interface UserDTO extends BaseEntity {
  username: string
  nickname?: string
  avatar?: string | null
  phoneNumber?: string
  sex?: number
  birthday?: string | null
  email?: string
  allowConcurrentLogin?: number
  deptId?: string | null
  deptName?: string | null
  status?: number
}

export interface UserCreateDTO {
  username: string
  nickname?: string
  avatar?: string | null
  phoneNumber?: string
  sex?: number
  birthday?: string | null
  email?: string
  allowConcurrentLogin?: number
  deptId?: string | null
  status?: number
}

export interface UserUpdateDTO {
  nickname?: string
  avatar?: string | null
  phoneNumber?: string
  sex?: number
  birthday?: string | null
  email?: string
  allowConcurrentLogin?: number
  deptId?: string | null
}

export interface UserPageQueryDTO extends PageQuery {
  username?: string
  deptId?: string
}

export interface RoleDTO extends BaseEntity {
  roleName: string
  roleCode: string
  appCode?: string
  dataScope?: number
  status?: number
  remark?: string
  rolePermissions?: PermissionDTO[]
}

export interface RolePageQueryDTO extends PageQuery {}

export interface RoleInfoVO {
  id: string
  roleName: string
  roleCode: string
  appCode?: string
  dataScope?: number
  status?: number
  userIds?: string[]
}

export interface RolePermissionRelationshipMappingDTO {
  permissionId: string
  roleId?: string
  expireTime?: string
}

export interface PermissionDTO extends BaseEntity {
  name: string
  identification: string
  parentId?: string | null
  identityLineage?: string
  comPath?: string
  path?: string
  iconStr?: string
  displayNo: number
  isFrame?: number
  type: number
  remark?: string
  status?: number
  children?: PermissionDTO[]
}

export interface PermissionVO extends PermissionDTO {}

export interface PermissionQueryDTO {
  username: string
  identifications?: string[]
  permissionTypes?: string[]
}

export interface DeptDTO extends BaseEntity {
  parentId?: string | null
  ancestors?: string
  deptName: string
  sortNum?: number
  status?: number
  children?: DeptDTO[]
}

export interface DeptQueryDTO extends PageQuery {
  parentId?: string
  deptName?: string
  status?: number
}

export interface PostDTO extends BaseEntity {
  postCode: string
  postName: string
  sortNum?: number
  status?: number
}

export interface PostQueryDTO extends PageQuery {
  postCode?: string
  postName?: string
  status?: number
}

export interface AppDTO extends BaseEntity {
  appCode: string
  appName: string
  status?: number
  userType?: number
  remark?: string
}

export interface AppQueryDTO extends PageQuery {
  appCode?: string
  appName?: string
  status?: number
  userType?: number
}

export interface ConfigDTO extends BaseEntity {
  configName: string
  configKey: string
  configValue: string
  configGroup: string
  sensitiveFlag: number
  status: number
  configType?: number
  remark?: string
}

export interface ConfigQueryDTO extends PageQuery {
  configName?: string
  configKey?: string
  configGroup?: string
  sensitiveFlag?: number
  status?: number
  configType?: number
}

export interface AdminMemberQueryDTO extends PageQuery {
  memberId?: string
  phoneNumber?: string
  nickname?: string
  email?: string
  status?: number
  realNameStatus?: number
  memberType?: number
  userLevel?: number
  phoneBound?: number
  hasWechatBind?: number
  registerStartTime?: string
  registerEndTime?: string
  lastLoginStartTime?: string
  lastLoginEndTime?: string
  registerIp?: string
  lastLoginIp?: string
}

export interface AdminMemberStatusUpdateDTO {
  reason: string
  expireTime?: string
  remark?: string
}

export interface AdminMemberForceLogoutDTO {
  reason: string
  remark?: string
}

export interface AdminMemberListVO extends BaseEntity {
  phoneNumber?: string
  nickname?: string
  avatar?: string | null
  status?: number
  realNameStatus?: number
  memberType?: number
  userLevel?: number
  points?: number
  phoneBound?: boolean
  wechatBound?: boolean
  lastLoginTime?: string
  registerIp?: string
  lastLoginIp?: string
}

export interface MemberSocialBindingVO extends BaseEntity {
  memberId: string
  identityType?: string
  identifier?: string
  unionId?: string
}

export interface MemberLoginAuditVO extends BaseEntity {
  userId?: string
  username?: string
  displayName?: string
  eventType?: string
  loginResult?: string
  failReason?: string
  loginIp?: string
  deviceType?: string
  clientType?: string
  loginTime?: string
  logoutTime?: string
}

export interface MemberManageRecordVO extends BaseEntity {
  memberId: string
  actionType?: number
  beforeStatus?: number
  afterStatus?: number
  reason?: string
  remark?: string
  expireTime?: string
  operatorId?: string
  operatorName?: string
  approveBy?: string
  approveTime?: string
}

export interface AdminMemberDetailVO extends BaseEntity {
  phoneNumber?: string
  nickname?: string
  avatar?: string | null
  sex?: number
  birthday?: string | null
  email?: string
  phoneBound?: boolean
  userLevel?: number
  points?: number
  memberType?: number
  realNameStatus?: number
  status?: number
  registerIp?: string
  lastLoginTime?: string
  lastLoginIp?: string
  profileExtra?: string
  wechatBound?: boolean
  statusReason?: string
  statusExpireTime?: string
  cancelled?: boolean
  cancelTime?: string
  disabledTime?: string
  registerSource?: string
  registerDevice?: string
  riskLevel?: number
  blacklistFlag?: number
  socialBindings?: MemberSocialBindingVO[]
  manageRecordSummary?: MemberManageRecordVO[]
  loginAuditSummary?: MemberLoginAuditVO[]
}

export interface DictTypeDTO extends BaseEntity {
  dictName: string
  dictType: string
  status?: number
  remark?: string
}

export interface DictTypeQueryDTO extends PageQuery {
  dictName?: string
  dictType?: string
  status?: number
}

export interface DictDataDTO extends BaseEntity {
  dictSort?: number
  dictLabel: string
  dictValue: string
  dictType: string
  status?: number
  remark?: string
}

export interface DictDataQueryDTO extends PageQuery {
  dictType?: string
  dictLabel?: string
  dictValue?: string
  status?: number
}
