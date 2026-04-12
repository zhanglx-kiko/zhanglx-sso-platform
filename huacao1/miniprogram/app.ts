// app.ts
import type { MemberSession } from './types/api'
import { ensureMemberSession, syncCurrentMember } from './utils/auth'
import { API_BASE_URL } from './utils/config'
import { clearStoredSession, getStoredMemberInfo, getStoredTokenName, getStoredTokenValue } from './utils/session'

App<IAppOption>({
  globalData: {
    apiBaseUrl: API_BASE_URL,
    ready: false,
    tokenName: getStoredTokenName(),
    tokenValue: getStoredTokenValue(),
    memberInfo: (getStoredMemberInfo() || undefined) as WechatMiniprogram.IAnyObject | undefined,
  },
  loginPromise: null,

  onLaunch() {
    void this.ensureMemberSession().catch(() => {
      this.globalData.ready = true
    })
  },

  async ensureMemberSession(forceRefresh = false) {
    if (this.loginPromise && !forceRefresh) {
      return this.loginPromise
    }

    const loginTask = ensureMemberSession(forceRefresh)
      .then((session) => {
        this.applySession(session)
        return session
      })
      .catch((error) => {
        if (forceRefresh) {
          this.clearMemberSession()
        }
        throw error
      })
      .finally(() => {
        this.loginPromise = null
        this.globalData.ready = true
      })

    this.loginPromise = loginTask
    return loginTask
  },

  async refreshCurrentMember() {
    const memberInfo = await syncCurrentMember()
    this.globalData.memberInfo = (memberInfo || undefined) as WechatMiniprogram.IAnyObject | undefined
    return memberInfo
  },

  clearMemberSession() {
    clearStoredSession()
    this.globalData.tokenName = ''
    this.globalData.tokenValue = ''
    this.globalData.memberInfo = undefined
  },

  applySession(session: MemberSession | null) {
    this.globalData.tokenName = session?.login?.tokenName || getStoredTokenName()
    this.globalData.tokenValue = session?.login?.tokenValue || getStoredTokenValue()
    this.globalData.memberInfo = (session?.memberInfo || undefined) as WechatMiniprogram.IAnyObject | undefined
  },

  onLaunchLegacyPlaceholder() {
    // 展示本地存储能力
    const logs = wx.getStorageSync('logs') || []
    logs.unshift(Date.now())
    wx.setStorageSync('logs', logs)

    // 登录
    wx.login({
      success: (res: WechatMiniprogram.LoginSuccessCallbackResult) => {
        console.log(res.code)
        // 发送 res.code 到后台换取 openId, sessionKey, unionId
      },
    })
  },
})
