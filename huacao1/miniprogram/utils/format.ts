import type { IdType, PlantCard, PublishStatus } from '../types/api'

export function formatPrice(value: number | string | undefined, unit?: string) {
  const numericValue = Number(value || 0)
  const priceText = Number.isFinite(numericValue) ? numericValue.toFixed(2) : '0.00'
  return `¥${priceText}${unit ? `/${unit}` : ''}`
}

export function formatDateTime(value?: string) {
  if (!value) {
    return '--'
  }
  return value.replace('T', ' ').slice(0, 16)
}

export function formatLocation(province?: string, city?: string, area?: string) {
  const parts = [province, city, area].filter((item) => !!item)
  return parts.length ? parts.join(' ') : '全国可售'
}

export function formatStatus(status: PublishStatus) {
  if (status === 0) {
    return '草稿'
  }
  if (status === 2) {
    return '下架'
  }
  return '上架'
}

export function formatCount(value?: number) {
  const count = Number(value || 0)
  if (count >= 10000) {
    return `${(count / 10000).toFixed(1)}w`
  }
  if (count >= 1000) {
    return `${(count / 1000).toFixed(1)}k`
  }
  return `${count}`
}

export function sameId(left?: IdType, right?: IdType) {
  if (!left || !right) {
    return false
  }
  return String(left) === String(right)
}

export function splitWaterfall(items: PlantCard[]) {
  const leftItems: PlantCard[] = []
  const rightItems: PlantCard[] = []
  items.forEach((item, index) => {
    if (index % 2 === 0) {
      leftItems.push(item)
      return
    }
    rightItems.push(item)
  })
  return {
    leftItems,
    rightItems,
  }
}
