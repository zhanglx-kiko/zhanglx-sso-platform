import type { PlantDetail, PlantFormData, PlantSavePayload, PublishStatus } from '../types/api'
import { COMMON_UNITS } from './config'

export function createEmptyPlantForm(): PlantFormData {
  return {
    categoryId: '',
    title: '',
    suggestedRetailPrice: '',
    unit: COMMON_UNITS[0],
    shortDescription: '',
    detailDescription: '',
    province: '',
    city: '',
    area: '',
    publishStatus: 1,
    coverImageUrl: '',
    images: [],
  }
}

export function normalizePriceInput(value: string) {
  let nextValue = value.replace(/[^\d.]/g, '')
  const dotIndex = nextValue.indexOf('.')
  if (dotIndex >= 0) {
    nextValue = `${nextValue.slice(0, dotIndex + 1)}${nextValue.slice(dotIndex + 1).replace(/\./g, '')}`
    const decimalPart = nextValue.slice(dotIndex + 1, dotIndex + 3)
    nextValue = `${nextValue.slice(0, dotIndex)}.${decimalPart}`
  }
  return nextValue
}

export function ensureCoverImage(imageUrls: string[], coverImageUrl?: string) {
  if (!imageUrls.length) {
    return ''
  }
  if (coverImageUrl && imageUrls.includes(coverImageUrl)) {
    return coverImageUrl
  }
  return imageUrls[0]
}

export function validatePlantForm(form: PlantFormData) {
  if (!form.categoryId) {
    return '请选择花草苗木分类'
  }
  if (!form.title.trim()) {
    return '请输入花草苗木名称'
  }
  if (!form.suggestedRetailPrice.trim() || Number(form.suggestedRetailPrice) <= 0) {
    return '请输入有效的建议零售价'
  }
  if (!form.unit.trim()) {
    return '请输入价格单位'
  }
  if (!form.shortDescription.trim()) {
    return '请输入简要描述'
  }
  if (!form.detailDescription.trim()) {
    return '请输入详细描述'
  }
  if (!form.images.length) {
    return '请至少上传一张图片'
  }
  return ''
}

export function buildPlantPayload(form: PlantFormData, publishStatus: PublishStatus): PlantSavePayload {
  const imageUrls = form.images.map((item) => item.url)
  return {
    categoryId: form.categoryId,
    title: form.title.trim(),
    suggestedRetailPrice: Number(form.suggestedRetailPrice).toFixed(2),
    unit: form.unit.trim(),
    shortDescription: form.shortDescription.trim(),
    detailDescription: form.detailDescription.trim(),
    province: form.province.trim() || undefined,
    city: form.city.trim() || undefined,
    area: form.area.trim() || undefined,
    imageUrls,
    coverImageUrl: ensureCoverImage(imageUrls, form.coverImageUrl),
    publishStatus,
  }
}

export function detailToPlantForm(detail: PlantDetail): PlantFormData {
  const imageUrls = detail.imageList.map((item) => item.imageUrl)
  return {
    categoryId: detail.categoryId,
    title: detail.title || '',
    suggestedRetailPrice: `${detail.suggestedRetailPrice || ''}`,
    unit: detail.unit || COMMON_UNITS[0],
    shortDescription: detail.shortDescription || '',
    detailDescription: detail.detailDescription || '',
    province: detail.province || '',
    city: detail.city || '',
    area: detail.area || '',
    publishStatus: detail.publishStatus,
    coverImageUrl: ensureCoverImage(imageUrls, detail.coverImageUrl),
    images: detail.imageList.map((item) => ({
      url: item.imageUrl,
    })),
  }
}
