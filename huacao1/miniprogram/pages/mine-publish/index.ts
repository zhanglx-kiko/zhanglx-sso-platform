import type { PlantCard, PlantMineSummary, PublishStatus } from '../../types/api'
import { deletePlantItem, getMyPlantSummary, pageMyPlantItems, updatePlantPublishStatus } from '../../utils/plant-service'

type StatusFilter = 'ALL' | 'PUBLISHED' | 'DRAFT' | 'OFF_SHELF'

Page({
  data: {
    loading: true,
    loadingMore: false,
    hasMore: true,
    pageNum: 1,
    pageSize: 10,
    activeStatus: 'ALL' as StatusFilter,
    statusTabs: [
      { label: '全部', value: 'ALL' },
      { label: '上架', value: 'PUBLISHED' },
      { label: '草稿', value: 'DRAFT' },
      { label: '下架', value: 'OFF_SHELF' },
    ],
    items: [] as PlantCard[],
    summary: {
      totalCount: 0,
      publishedCount: 0,
      draftCount: 0,
      offShelfCount: 0,
    } as PlantMineSummary,
  },

  onShow() {
    void this.bootstrap()
  },

  onPullDownRefresh() {
    void this.bootstrap()
  },

  onReachBottom() {
    if (this.data.loading || this.data.loadingMore || !this.data.hasMore) {
      return
    }
    void this.loadList(false)
  },

  async bootstrap() {
    this.setData({
      loading: true,
    })
    await Promise.all([this.loadSummary(), this.loadList(true)])
  },

  async loadSummary() {
    try {
      const summary = await getMyPlantSummary()
      this.setData({
        summary,
      })
    } catch (error) {
      this.showToast((error as Error).message)
    }
  },

  async loadList(reset = false) {
    const nextPageNum = reset ? 1 : this.data.pageNum
    this.setData({
      loading: reset,
      loadingMore: !reset,
    })
    try {
      const pageData = await pageMyPlantItems({
        pageNum: nextPageNum,
        pageSize: this.data.pageSize,
        publishStatus: this.resolvePublishStatus(),
      })
      const mergedItems = reset ? pageData.records : this.data.items.concat(pageData.records)
      this.setData({
        items: mergedItems,
        pageNum: nextPageNum + 1,
        hasMore: mergedItems.length < pageData.total,
      })
    } catch (error) {
      this.showToast((error as Error).message)
    } finally {
      this.setData({
        loading: false,
        loadingMore: false,
      })
      wx.stopPullDownRefresh()
    }
  },

  onSelectStatus(event: any) {
    const activeStatus = String(event.currentTarget.dataset.value || 'ALL') as StatusFilter
    this.setData({
      activeStatus,
    })
    void this.loadList(true)
  },

  openDetail(event: any) {
    const item = event.detail
    if (!item?.id) {
      return
    }
    wx.navigateTo({
      url: `/pages/detail/index?id=${item.id}`,
    })
  },

  goPublish() {
    wx.navigateTo({
      url: '/pages/publish/index',
    })
  },

  editItem(event: any) {
    const itemId = String(event.currentTarget.dataset.id || '')
    if (!itemId) {
      return
    }
    wx.navigateTo({
      url: `/pages/edit/index?id=${itemId}`,
    })
  },

  async togglePublishStatus(event: any) {
    const itemId = String(event.currentTarget.dataset.id || '')
    const currentStatus = Number(event.currentTarget.dataset.status || 0) as PublishStatus
    if (!itemId) {
      return
    }
    const nextStatus: PublishStatus = currentStatus === 1 ? 2 : 1
    const actionText = nextStatus === 1 ? '上架' : '下架'
    const confirmed = await this.confirmAction(`确认${actionText}这条内容吗？`)
    if (!confirmed) {
      return
    }
    try {
      await updatePlantPublishStatus(itemId, nextStatus)
      wx.showToast({
        title: `${actionText}成功`,
        icon: 'success',
      })
      await Promise.all([this.loadSummary(), this.loadList(true)])
    } catch (error) {
      this.showToast((error as Error).message)
    }
  },

  async deleteItem(event: any) {
    const itemId = String(event.currentTarget.dataset.id || '')
    if (!itemId) {
      return
    }
    const confirmed = await this.confirmAction('删除后内容将不再展示，确认继续吗？')
    if (!confirmed) {
      return
    }
    try {
      await deletePlantItem(itemId)
      wx.showToast({
        title: '删除成功',
        icon: 'success',
      })
      await Promise.all([this.loadSummary(), this.loadList(true)])
    } catch (error) {
      this.showToast((error as Error).message)
    }
  },

  resolvePublishStatus() {
    if (this.data.activeStatus === 'PUBLISHED') {
      return 1 as PublishStatus
    }
    if (this.data.activeStatus === 'DRAFT') {
      return 0 as PublishStatus
    }
    if (this.data.activeStatus === 'OFF_SHELF') {
      return 2 as PublishStatus
    }
    return undefined
  },

  confirmAction(content: string) {
    return new Promise<boolean>((resolve) => {
      wx.showModal({
        title: '提示',
        content,
        confirmColor: '#355339',
        success: (result) => {
          resolve(!!result.confirm)
        },
        fail: () => resolve(false),
      })
    })
  },

  showToast(title: string) {
    wx.showToast({
      title: title || '请稍后重试',
      icon: 'none',
    })
  },
})
