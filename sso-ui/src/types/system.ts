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
  configType?: number
  remark?: string
}

export interface ConfigQueryDTO extends PageQuery {
  configName?: string
  configKey?: string
  configType?: number
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
