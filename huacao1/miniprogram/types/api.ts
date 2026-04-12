export type IdType = string

export type PublishStatus = 0 | 1 | 2

export interface ApiEnvelope<T> {
  code: number
  msg: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages?: number
}

export interface MemberLoginVO {
  id: IdType
  username?: string
  nickname?: string
  avatar?: string
  deptId?: IdType
  tokenName: string
  tokenValue: string
}

export interface MemberInfo {
  id: IdType
  phoneNumber?: string
  nickname?: string
  avatar?: string
  phoneBound?: boolean
  userLevel?: number
  points?: number
  status?: number
  createTime?: string
  updateTime?: string
}

export interface MemberSession {
  login: MemberLoginVO | null
  memberInfo: MemberInfo | null
}

export interface PlantCategory {
  id: IdType
  categoryName: string
  categoryCode: string
  description?: string
}

export interface PlantPublisher {
  id: IdType
  nickname: string
  avatar?: string
}

export interface PlantImage {
  imageUrl: string
  cover: boolean
  sortNum: number
}

export interface PlantCard {
  id: IdType
  categoryId: IdType
  categoryName: string
  title: string
  coverImageUrl: string
  suggestedRetailPrice: number | string
  unit: string
  shortDescription: string
  province?: string
  city?: string
  viewCount: number
  publishStatus: PublishStatus
  publisherName?: string
  publisherAvatar?: string
  createTime?: string
  updateTime?: string
}

export interface PlantDetail extends PlantCard {
  detailDescription: string
  area?: string
  imageList: PlantImage[]
  publisher?: PlantPublisher
}

export interface PlantMineSummary {
  totalCount: number
  publishedCount: number
  draftCount: number
  offShelfCount: number
}

export interface UploadImageResult {
  fileName: string
  url: string
  size: number
}

export interface PlantListQuery {
  pageNum: number
  pageSize: number
  categoryId?: IdType
  searchKey?: string
  province?: string
  city?: string
  publishStatus?: PublishStatus
}

export interface PlantFormImage {
  url: string
  size?: number
  fileName?: string
}

export interface PlantFormData {
  categoryId: IdType
  title: string
  suggestedRetailPrice: string
  unit: string
  shortDescription: string
  detailDescription: string
  province: string
  city: string
  area: string
  publishStatus: PublishStatus
  coverImageUrl: string
  images: PlantFormImage[]
}

export interface PlantSavePayload {
  categoryId: IdType
  title: string
  suggestedRetailPrice: string
  unit: string
  shortDescription: string
  detailDescription: string
  province?: string
  city?: string
  area?: string
  imageUrls: string[]
  coverImageUrl?: string
  publishStatus: PublishStatus
}
