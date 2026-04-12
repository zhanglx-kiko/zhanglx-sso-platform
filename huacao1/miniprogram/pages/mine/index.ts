import type { MemberInfo, PlantMineSummary } from '../../types/api'
import { getStoredMemberInfo } from '../../utils/session'
import { getMyPlantSummary } from '../../utils/plant-service'

Page({
  data: {
    loading: true,
    memberInfo: (getStoredMemberInfo() || null) as MemberInfo | null,
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
    void this.bootstrap(true)
  },

  async bootstrap(forceRefresh = false) {
    this.setData({
      loading: true,
    })
    try {
      const app = getApp<IAppOption>()
      const session = await app.ensureMemberSession(forceRefresh)
      const memberInfo = (session?.memberInfo || app.globalData.memberInfo || null) as MemberInfo | null
      const summary = await getMyPlantSummary()
      this.setData({
        memberInfo,
        summary,
      })
    } catch (error) {
      this.showToast((error as Error).message)
    } finally {
      this.setData({
        loading: false,
      })
      wx.stopPullDownRefresh()
    }
  },

  goMyPublish() {
    wx.navigateTo({
      url: '/pages/mine-publish/index',
    })
  },

  goPublish() {
    wx.navigateTo({
      url: '/pages/publish/index',
    })
  },

  goHome() {
    wx.reLaunch({
      url: '/pages/home/index',
    })
  },

  syncAccount() {
    void this.bootstrap(true)
  },

  showToast(title: string) {
    wx.showToast({
      title: title || '请稍后重试',
      icon: 'none',
    })
  },
})
