import type {
  PageResult,
  PlantCard,
  PlantCategory,
  PlantDetail,
  PlantListQuery,
  PlantMineSummary,
  PlantSavePayload,
  PublishStatus,
  UploadImageResult,
} from '../types/api'
import { request, uploadFile } from './request'

export function listPlantCategories() {
  return request<PlantCategory[]>({
    url: '/apis/v1/horticultural-plants/m/categories',
  })
}

export function pagePlantItems(query: PlantListQuery) {
  return request<PageResult<PlantCard>, PlantListQuery>({
    url: '/apis/v1/horticultural-plants/m/items',
    data: query,
  })
}

export function getPlantDetail(itemId: string) {
  return request<PlantDetail>({
    url: `/apis/v1/horticultural-plants/m/items/${itemId}`,
  })
}

export function pageMyPlantItems(query: PlantListQuery) {
  return request<PageResult<PlantCard>, PlantListQuery>({
    url: '/apis/v1/horticultural-plants/m/items/mine',
    data: query,
    auth: true,
  })
}

export function getMyPlantSummary() {
  return request<PlantMineSummary>({
    url: '/apis/v1/horticultural-plants/m/items/mine/summary',
    auth: true,
  })
}

export function createPlantItem(payload: PlantSavePayload) {
  return request<PlantDetail, PlantSavePayload>({
    url: '/apis/v1/horticultural-plants/m/items',
    method: 'POST',
    data: payload,
    auth: true,
  })
}

export function updatePlantItem(itemId: string, payload: PlantSavePayload) {
  return request<PlantDetail, PlantSavePayload>({
    url: `/apis/v1/horticultural-plants/m/items/${itemId}`,
    method: 'PUT',
    data: payload,
    auth: true,
  })
}

export function updatePlantPublishStatus(itemId: string, publishStatus: PublishStatus) {
  return request<void, { publishStatus: PublishStatus }>({
    url: `/apis/v1/horticultural-plants/m/items/${itemId}/publish-status`,
    method: 'PATCH',
    data: {
      publishStatus,
    },
    auth: true,
  })
}

export function deletePlantItem(itemId: string) {
  return request<void>({
    url: `/apis/v1/horticultural-plants/m/items/${itemId}`,
    method: 'DELETE',
    auth: true,
  })
}

export async function uploadPlantImages(filePaths: string[]) {
  const result: UploadImageResult[] = []
  for (const filePath of filePaths) {
    const uploadResult = await uploadFile<UploadImageResult[]>({
      url: '/apis/v1/horticultural-plants/m/items/upload-images',
      filePath,
      name: 'files',
      auth: true,
    })
    result.push(...uploadResult)
  }
  return result
}
