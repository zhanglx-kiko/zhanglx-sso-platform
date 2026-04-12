import type { PlantCard, PlantCategory } from '../../types/api'
import { splitWaterfall } from '../../utils/format'
import { listPlantCategories, pagePlantItems } from '../../utils/plant-service'

Page({
  data: {
    categories: [] as PlantCategory[],
    activeCategoryId: '',
    searchDraft: '',
    keyword: '',
    items: [] as PlantCard[],
    leftItems: [] as PlantCard[],
    rightItems: [] as PlantCard[],
    loading: true,
    loadingMore: false,
    hasMore: true,
    pageNum: 1,
    pageSize: 10,
  },

  onLoad() {
    void this.bootstrap()
  },

  onPullDownRefresh() {
    void this.loadList(true)
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
    await Promise.all([this.loadCategories(), this.loadList(true)])
    void getApp<IAppOption>().ensureMemberSession().catch(() => void 0)
  },

  async loadCategories() {
    try {
      const categories = await listPlantCategories()
      this.setData({
        categories,
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
      const pageData = await pagePlantItems({
        pageNum: nextPageNum,
        pageSize: this.data.pageSize,
        categoryId: this.data.activeCategoryId || undefined,
        searchKey: this.data.keyword || undefined,
      })
      const mergedItems = reset ? pageData.records : this.data.items.concat(pageData.records)
      const waterfall = splitWaterfall(mergedItems)
      this.setData({
        items: mergedItems,
        leftItems: waterfall.leftItems,
        rightItems: waterfall.rightItems,
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

  onSearchInput(event: any) {
    this.setData({
      searchDraft: event.detail.value,
    })
  },

  onSearchConfirm() {
    this.setData({
      keyword: this.data.searchDraft.trim(),
    })
    void this.loadList(true)
  },

  clearSearch() {
    this.setData({
      searchDraft: '',
      keyword: '',
    })
    void this.loadList(true)
  },

  onSelectCategory(event: any) {
    const categoryId = String(event.currentTarget.dataset.id || '')
    this.setData({
      activeCategoryId: categoryId,
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

  goMine() {
    wx.navigateTo({
      url: '/pages/mine/index',
    })
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

  showToast(title: string) {
    wx.showToast({
      title: title || '请稍后重试',
      icon: 'none',
    })
  },
})
