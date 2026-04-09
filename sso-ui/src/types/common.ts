export type EntityId = string

export interface SortingField {
  field: string
  order: 'asc' | 'desc'
}

export interface PageQuery {
  pageNum: number
  pageSize: number
  searchKey?: string
  sortingFields?: SortingField[]
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

export interface BaseEntity {
  id: EntityId
  createBy?: EntityId
  createTime?: string
  updateBy?: EntityId
  updateTime?: string
}

export interface StatusPayload {
  status: number
}

export interface SelectOption<T = string | number> {
  label: string
  value: T
  disabled?: boolean
}

export interface TransferOption {
  key: string
  label: string
  disabled?: boolean
}
