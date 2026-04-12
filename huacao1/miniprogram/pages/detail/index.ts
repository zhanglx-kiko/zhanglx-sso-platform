import type { MemberInfo, PlantDetail } from '../../types/api'
import { formatDateTime, formatLocation, formatPrice, sameId } from '../../utils/format'
import { getPlantDetail } from '../../utils/plant-service'

Page({
  data: {
    itemId: '',
    item: null as PlantDetail | null,
    loading: true,
    isMine: false,
    previewUrls: [] as string[],
    locationText: '--',
    publishTime: '--',
    formattedPrice: '¥0.00',
  },

  onLoad(options: any) {
    const itemId = String(options.id || '')
    if (!itemId) {
      this.showToast('缺少内容编号')
      return
    }
    this.setData({
      itemId,
    })
    void this.loadDetail()
  },

  async loadDetail() {
    this.setData({
      loading: true,
    })
    try {
      const detail = await getPlantDetail(this.data.itemId)
      const previewUrls = detail.imageList.length ? detail.imageList.map((item) => item.imageUrl) : [detail.coverImageUrl]
      const memberInfo = getApp<IAppOption>().globalData.memberInfo as MemberInfo | undefined
      this.setData({
        item: detail,
        previewUrls,
        isMine: sameId(memberInfo?.id, detail.publisher?.id),
        locationText: formatLocation(detail.province, detail.city, detail.area),
        publishTime: formatDateTime(detail.createTime),
        formattedPrice: formatPrice(detail.suggestedRetailPrice, detail.unit),
      })
    } catch (error) {
      this.showToast((error as Error).message)
    } finally {
      this.setData({
        loading: false,
      })
    }
  },

  previewImage(event: any) {
    const current = String(event.currentTarget.dataset.url || '')
    if (!current) {
      return
    }
    wx.previewImage({
      current,
      urls: this.data.previewUrls,
    })
  },

  async goMyPublish() {
    try {
      await getApp<IAppOption>().ensureMemberSession()
      wx.navigateTo({
        url: '/pages/mine-publish/index',
      })
    } catch (error) {
      this.showToast((error as Error).message)
    }
  },

  async goPublish() {
    try {
      await getApp<IAppOption>().ensureMemberSession()
      wx.navigateTo({
        url: '/pages/publish/index',
      })
    } catch (error) {
      this.showToast((error as Error).message)
    }
  },

  goEdit() {
    if (!this.data.item?.id) {
      return
    }
    wx.navigateTo({
      url: `/pages/edit/index?id=${this.data.item.id}`,
    })
  },

  showToast(title: string) {
    wx.showToast({
      title: title || '请稍后重试',
      icon: 'none',
    })
  },
})
