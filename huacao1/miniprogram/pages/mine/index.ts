import type { MemberInfo, PlantMineSummary } from '../../types/api'
import { getMyPlantSummary } from '../../utils/plant-service'
import { getStoredMemberInfo } from '../../utils/session'

function createEmptySummary(): PlantMineSummary {
  return {
    totalCount: 0,
    publishedCount: 0,
    draftCount: 0,
    offShelfCount: 0,
  }
}

Page({
  data: {
    loading: true,
    loggingIn: false,
    needsLogin: false,
    loginErrorMessage: '',
    memberInfo: (getStoredMemberInfo() || null) as MemberInfo | null,
    summary: createEmptySummary() as PlantMineSummary,
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
      ...(forceRefresh ? { loggingIn: true } : {}),
    })
    const app = getApp<IAppOption>()
    try {
      const session = await app.ensureMemberSession(forceRefresh)
      const memberInfo = (session?.memberInfo || app.globalData.memberInfo || null) as MemberInfo | null
      try {
        const summary = await getMyPlantSummary()
        this.setData({
          memberInfo,
          summary,
          needsLogin: false,
          loginErrorMessage: '',
        })
      } catch (error) {
        this.setData({
          memberInfo,
          summary: createEmptySummary(),
          needsLogin: false,
          loginErrorMessage: '',
        })
        this.showToast((error as Error).message)
      }
    } catch (error) {
      this.setData({
        memberInfo: null,
        summary: createEmptySummary(),
        needsLogin: true,
        loginErrorMessage: (error as Error).message || '请先完成微信登录',
      })
    } finally {
      this.setData({
        loading: false,
        loggingIn: false,
      })
      wx.stopPullDownRefresh()
    }
  },

  async handleWechatLogin() {
    if (this.data.loggingIn) {
      return
    }
    await this.bootstrap(true)
    if (!this.data.needsLogin) {
      wx.showToast({
        title: '登录成功',
        icon: 'success',
      })
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
